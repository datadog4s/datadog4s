---
layout: docs 
title:  "User guide"
position: 2
---

# User guide

- [Creating metric factory](#creating-metric-factory)
- [Creating metrics](#creating-metrics)
- [Timers](#timers)
- [Tagging](#tagging)
    - [Tagger](#tagger)
- [Extensions](#extensions)
    - [Http4s](#http4s)
    - [Jvm monitoring](#jvm-monitoring)

## Creating metric factory

To start creating your metrics, first you need to create a `MetricFactory[F[_]]`. Currently, the only implementation is
in `statsd` package. MetricFactory is purely functional, so it requires you to provide type constructor which
implements `cats.effect.Sync`. For the simplicity, we will use `cats.effect.IO` in these examples.

To create an instance, we need to provide it with configuration which contains a few basic fields, like address of the
StatsD server, prefix etc. For more information see scaladoc of the config class.

The instance is wrapped in `Resource` because of the underlying `StatsD` client.

```scala mdoc:silent
import java.net.InetSocketAddress
import cats.effect.*
import io.github.datadog4s.api.*
import io.github.datadog4s.api.metric.*
import io.github.datadog4s.*

val statsDServer = InetSocketAddress.createUnresolved("localhost", 8125)
val config = StatsDMetricFactoryConfig(Some("my-app-name"), statsDServer)

val factoryResource: Resource[IO, MetricFactory[IO]] = StatsDMetricFactory.make(config)
```

## Creating metrics

Once you have a metrics factory, creating metrics is straight-forward. Note that all metric operations return
side-effecting actions.

```scala mdoc:silent
factoryResource.use { factory =>
    val count: Count[IO] = factory.count("hits")
    val histogram: Histogram[IO, Long] = factory.histogram.long("my-histogram")
    for {
        _ <-  count.inc() // increase count by one
        _ <- histogram.record(1337, Tag.of("username", "xyz")) // record a value to histogram with Tag
    } yield {
        ()
    }
}
```

## Timers

In addition to basic datadog metrics, we provide a `Timer[F]` abstraction which has proved to be very useful is
practice. Timers provide you with `.time[A](fa: F[A]): F[A]` method, which will measure how long it took to run
provided `fa`. In addition, it tags the metric with `success:true` or `success:false`
and `exception:<<throwable class name>>` in case the `fa` failed.

In addition to `.time[A]` method it also allows for recording of raw values that represent elapsed time or even raw time
data. You can see example of such calls below.

Optionally, when creating a `Timer`, you can also set the time units which will be used for reporting. By default, all
timers create with `microsecond` granularity. You can provide your own time unit if you need more or less precision.

### Histogram vs Distribution

There are two versions of timers, one backed by `Histogram` and one backed by `Distribution`. You can read scaladoc for
more details and links to datadog documentation.

Long story short, `histogram` backed timers are aggregated per datadog agent, while the `distributions` are computed on
datadog server. The implications are that `distribution` based timers, and it's buckets (50th, 75th, 95th percentile
etc) are more correct and in general it's the implementation that we'd suggest to use.

### Example

```scala mdoc:silent
import java.util.concurrent.TimeUnit

factoryResource.use { factory =>
    val timer = factory.timer.distribution("request-latency")
    
    timer.time(IO.delay(println("success"))) // tagged with success:true
    timer.time(IO.raiseError(new NullPointerException("error"))) // tagged with success:false and exception:NullPointerException
    
    val nanoTimer = factory.timer.distribution("nano-timer", timeUnit = TimeUnit.NANOSECONDS)
    nanoTimer.time(IO.delay(println("success"))) // metric will be recorded with 'nanoseconds' precision
    
    // timer.record works for all types that implement `ElapsedTime` typeclass, out of the box we provide implementation
    // for java.time.Duration and scala.concurrent.duration.FiniteDuration
    
    import java.time.Duration
    timer.record(Duration.ofNanos(1000))
    
    import scala.concurrent.duration.FiniteDuration
    timer.record(FiniteDuration(1000, TimeUnit.MILLISECONDS))
    
    timer.recordTime(1000L, TimeUnit.MILLISECONDS)
}
```

## Tagging

There are two ways to create a `Tag` instances. One way is using `of` method of `Tag` object, like so:

```scala mdoc
import io.github.datadog4s.api.Tag

Tag.of("endpoint", "admin/login")
```

This is simple and straight-forward, but in some cases it leaves your code with `Tag` keys scattered around and forces
you to repeat it - making it prone to misspells etc. The better way is to use `Tagger`.

### Tagger

`Tagger[T]` is basically a factory interface for creating tags based on provided value of type `T` - as long as
implicit `TagValue[T]` exists in scope. This instance is used for converting `T` into `String`. By using `Tagger`, you
get a single value that you can use in multiple places in your code to create `Tag`s without repeating yourself.

Example:

```scala mdoc
import io.github.datadog4s.api.tag.{TagValue, Tagger}

val pathTagger: Tagger[String] = Tagger.make[String]("path")
assert(Tag.of("path", "admin/login") == pathTagger.tag("admin/login"))

// tagger also supports taging using custom types using TagValue typeclass

case class StatusCode(value: Int)

implicit val statusCodeTagValue: TagValue[StatusCode] = TagValue[Int].contramap[StatusCode](sc => sc.value)

val statusCodeTagger: Tagger[StatusCode] = Tagger.make[StatusCode]("statusCode")

assert(Tag.of("statusCode", "200") == statusCodeTagger.tag(StatusCode(200)))
```

## Extensions

Extensions are packages that monitor some functionality for you - without you having to do much.

### Http4s

Http4s package (`datadog4s-http4s`) provides implementation of [MetricsOps](metrics-ops) that is used
by [http4s](http4s) to report both client and server metrics.

```scala mdoc
import io.github.datadog4s.extension.http4s.*

factoryResource.use { metricFactory =>
  // create metrics factory and use it as you please
  DatadogMetricsOps.builder[IO](metricFactory).build().flatMap { metricOps =>
    // setup http4s Metrics middleware here
    val _ = metricOps
    IO.unit
  }
}
```

### Jvm monitoring

JVM monitoring package (`datadog4s-jvm`) collects a bunch of JVM metrics that we found useful over last 5 or so years
running JVM apps in Avast. Those metrics can be found in [JvmReporter][jvm-reporter-class] and are hopefully
self-explanatory. We tried to match reported metrics to [datadog JVM runtime metrics][ddog-jvm-metrics]

Usage can not be simpler (unless you want to configure things like collection-frequency etc.). Simply add following to
your initialization code. Resource is returned, because a fiber is started in the background and has to be terminated
eventually.

```scala mdoc:silent
import io.github.datadog4s.extension.jvm.*

val jvmMonitoring: Resource[IO, Unit] = factoryResource.flatMap {
  factory => JvmMonitoring.default[IO](factory)
}

jvmMonitoring.use { _ => 
    // your application is in here
    IO.unit
}
```

#### Note on Java compatibility:

Starting with Java 16, applications that use our jvm monitoring need to
add `--add-opens=java.management/sun.management=ALL-UNNAMED` as JVM parameter when starting the application. This is
because JVM monitoring uses internal java APIs to obtain certain metrics.

[jvm-reporter-class]: https://github.com/datadog4s/datadog4s/blob/master/code/jvm/src/main/scala/io/github/datadog4s/datadog4s/extension/jvm/JvmReporter.scala

[metrics-ops]: https://http4s.org/v0.21/api/org/http4s/metrics/metricsops

[http4s]: https://http4s.org

[ddog-jvm-metrics]: https://docs.datadoghq.com/tracing/runtime_metrics/java/
