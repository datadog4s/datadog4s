package io.github.datadog4s.playground

import cats.effect.{ExitCode, IO, IOApp}
import io.github.datadog4s.api.metric.{Distribution, Histogram}
import io.github.datadog4s.{StatsDMetricFactory, StatsDMetricFactoryConfig}

import java.net.InetSocketAddress

object CreateMetric extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    StatsDMetricFactory
      .make[IO](StatsDMetricFactoryConfig(Some("playground"), new InetSocketAddress("127.0.0.1", 8125)))
      .use { factory =>
        for {
          _ <- IO.unit
          dist = factory.distribution.long("distribution1")
          hist = factory.histogram.long("histogram1")
          _ <- loop(hist, dist)
        } yield ExitCode.Success
      }
  def loop(hist: Histogram[IO, Long], dist: Distribution[IO, Long]): IO[Unit] = {
    val l = scala.util.Random.nextInt(100)
    import scala.concurrent.duration.*
    val p = for {
      _ <- hist.record(l.toLong)
      _ <- dist.record(l.toLong)
      _ <- IO.sleep(100.milli)
    } yield ()
    p.flatMap(_ => loop(hist, dist))
  }
}
