---
layout: docs
title:  "User guide"
position: 1
---

# User guide

- [User guide](#user-guide)
  - [Quick start](#quick-start)
    - [Installation](#installation)
    - [Creating metric factory](#creating-metric-factory)
    - [Creating metrics](#creating-metrics)
    - [Timers](#timers)
    - [Tagging](#tagging)
      - [Tagger](#tagger)
  - [Extensions](#extensions)
    - [Http4s](#http4s)
    - [Jvm monitoring](#jvm-monitoring)

## Quick start

### Installation
To start monitoring your code, first you need to add this library as a dependency to your project. This project is composed of multiple packages to make it easy for you to pick and choose what you require. 

You need to add `datadog4s-api` which contains classes defining our API. You also need to add it's implementation. Currently we only support metric delivery using StatsD in package `datadog4s` which already contains `api`. We are going to assume you are using `sbt`:

```scala
libraryDependencies += "com.avast.cloud" %% "datadog4s-api" % "@VERSION@"
```

### Creating metric factory
To start creating your metrics, first you need to create a `MetricFactory[F[_]]`. Currently the only implementation is in `statsd` package. MetricFactory is purely functional so it requires you to provide type constructor which implements `cats.effect.Sync`. For the simplicity, we will use `cats.effect.IO` in these examples.

To create an instance, we need to provide it with configuration which contains a few basic fields, like address of the StatsD server, prefix etc. For more information see scaladoc of the config class.

The instance is wrapped in `Resource` because of the underlying `StatsD` client.

```scala mdoc:silent
import java.net.InetSocketAddress
import cats.effect._
import com.avast.datadog4s.api._
import com.avast.datadog4s.api.metric._
import com.avast.datadog4s._

val statsDServer = InetSocketAddress.createUnresolved("localhost", 8125)
val config = StatsDMetricFactoryConfig("my-app-name", statsDServer)

val factoryResource: Resource[IO, MetricFactory[IO]] = StatsDMetricFactory.make(config)
```

### Creating metrics
Once you have a metrics factory, creating metrics is straight forward.

```scala mdoc:silent
factoryResource.use { factory =>
    val count: Count[IO] = factory.count("hits")
    count.inc() // increase count by one

    val histogram: Histogram[IO, Long] = factory.histogram.long("my-histogram")
    histogram.record(1337, Tag.of("username", "xyz")) // record a value to histogram with Tag
}
```

### Timers
Timers are great. And with our API, they are even better. Because we are living in functional code, we expect you to provide us with `F[_]: Sync` and we will time how long execution takes, and tag it with whether it succeeded (and if it failed, which class of exception was thrown).

```scala mdoc:silent

factoryResource.use { factory =>
    val timer = factory.timer("request-latency")

    timer.time(IO.pure(println("success"))) // tagged as success
    timer.time(IO.raiseError(new Exception("error"))) //tagged as failure
}
```

### Tagging
There are two ways to create a `Tag` instances. One way is using `of` method of `Tag` object, like so:
```scala mdoc
import com.avast.datadog4s.api.Tag

Tag.of("endpoint", "admin/login")
```
This is simple and straight-forward, but in some cases it leaves your code with `Tag` keys scattered around in your code and forces you to repeat it - making it prone to misspells etc. The better way is to use `Tagger`. 

#### Tagger
`Tagger[T]` is basically a factory interface for creating tags based on provided value of type `T` - as long as implicit `TagValue[T]` exists in scope. This instance is used for converting `T` into `String`. By using `Tagger`, you get a single value that you can use in multiple places in your code to create `Tag`s without repeating yourself.

Example: 
```scala mdoc
import com.avast.datadog4s.api.tag.{TagValue, Tagger}

case class StatusCode(value: Int)

implicit val statusCodeTagValue: TagValue[StatusCode] = TagValue[Int].contramap[StatusCode](sc => sc.value)

val pathTagger: Tagger[String] = Tagger.make[String]("path")
val statusCodeTagger: Tagger[StatusCode] = Tagger.make[StatusCode]("statusCode")

assert(Tag.of("path", "admin/login") == pathTagger.tag("admin/login"))
assert(Tag.of("statusCode", "200") == statusCodeTagger.tag(StatusCode(200)))
```


## Extensions
Extensions are packages that monitor some functionality for you - without you having to do anything.

### Http4s
Http4s package (`datadog4s-http4s`) provides implementation of [MetricsOps](metrics-ops) that is used by [http4s](http4s) to report both client and server metrics.

```scala mdoc:silent
import com.avast.datadog4s.extension.http4s._

factoryResource.use { metricFactory =>
    val _ = DatadogMetricsOps.make[IO](metricFactory) // create metrics factory and use it as you please
    IO.pure(())
}
```

### Jvm monitoring
JVM monitoring package (`datadog4s-jvm`) collects bunch of JVM metrics that we found useful over last 5 or so years running JVM apps in Avast. Those metrics can be found in [JvmReporter][jvm-reporter-class] and are hopefully self explenatory. 

Usage can not be simpler (unless you want to configure things like collection-frequency etc.). Simply add following to your initialization code. Resource is returned, because `Scheduler` has to be created which does the actual metric collection.

```scala mdoc:silent
import com.avast.datadog4s.extension.jvm._
import scala.concurrent.ExecutionContext

implicit val ec = ExecutionContext.global // please don't use global EC in production
implicit val contextShift = IO.contextShift(ec)
implicit val timer = IO.timer(ec)

val jvmMonitoring: Resource[IO, Unit] = factoryResource.flatMap(factory => JvmMonitoring.default[IO](factory))

jvmMonitoring.use { _ => 
    // your application is in here
    IO.pure(())
}
```

[jvm-reporter-class]: ../jvm/src/main/scala/com/avast/datadog4s/extension/jvm/JvmReporter.scala
[metrics-ops]: https://http4s.org/v0.20/api/org/http4s/metrics/metricsops
[http4s]: https://http4s.org
