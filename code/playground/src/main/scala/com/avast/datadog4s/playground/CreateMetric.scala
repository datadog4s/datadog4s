package com.avast.datadog4s.playground

import cats.Monad
import cats.effect.kernel.Ref
import cats.effect.{ ExitCode, IO, IOApp, Resource }
import com.avast.datadog4s.api.MetricFactory
import com.avast.datadog4s.api.metric.{ Distribution, Histogram }
import com.avast.datadog4s.extension.jvm.JvmMonitoring
import com.avast.datadog4s.{ StatsDMetricFactory, StatsDMetricFactoryConfig }
import scala.collection.compat.immutable._

import java.net.InetSocketAddress

object CreateMetric extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val task                = for {
      factory <- StatsDMetricFactory
                   .make[IO](StatsDMetricFactoryConfig(Some("playground"), new InetSocketAddress("127.0.0.1", 8125)))
      _       <- JvmMonitoring.default(factory)
      result  <- Resource.eval(doRun(factory))
    } yield result
    val userCancellableLoop = IO.race(task.use(_ => IO.unit), awaitUserInput)
    userCancellableLoop.map(_ => ExitCode.Success)
  }

  private def doRun(factory: MetricFactory[IO]): IO[Unit] =
    for {
      drawProgressBar <- initProgressBar
      dist             = factory.distribution.long("distribution1")
      hist             = factory.histogram.long("histogram1")
      _               <- loop(drawProgressBar, hist, dist)
    } yield ()

  private def awaitUserInput: IO[Unit] = IO.readLine.attempt.flatMap(_ => IO.unit)

  private def initProgressBar: IO[IO[Unit]] = {
    val progressBar = Ref.of[IO, LazyList[Char]](LazyList.continually("⣾⣽⣻⢿⡿⣟⣯⣷".to(LazyList)).flatten)
    progressBar.map { ref =>
      ref.modify(s => (s.tail, s.head)).flatMap(c => IO.print(s"\rRecording metrics:s $c"))
    }
  }

  def loop(drawProgressBar: IO[Unit], hist: Histogram[IO, Long], dist: Distribution[IO, Long]): IO[Unit] = {
    import scala.concurrent.duration._
    Monad[IO].foreverM {
      for {
        l <- IO.delay(scala.util.Random.nextInt(100))
        _ <- hist.record(l.toLong)
        _ <- dist.record(l.toLong)
        _ <- drawProgressBar
        _ <- IO.sleep(100.milli)
      } yield ()
    }
  }
}
