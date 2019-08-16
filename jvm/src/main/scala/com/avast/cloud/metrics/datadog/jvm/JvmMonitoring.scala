package com.avast.cloud.metrics.datadog.jvm

import java.time.Duration
import java.util.concurrent.{ Executors, ScheduledExecutorService, ThreadFactory, TimeUnit }

import cats.effect.{ Effect, IO, Resource, Sync }
import com.avast.cloud.metrics.datadog.api.MetricFactory

object JvmMonitoring {
  type ErrorHandler[F[_]] = Throwable => F[Unit]

  case class Config(initialDelay: Duration = Duration.ofMillis(0),
                    delay: Duration = Duration.ofMinutes(1),
                    schedulerThreadName: String = "datadog-jvm-reporter")

  def default[F[_]: Effect](factory: MetricFactory[F]): Resource[F, Unit] =
    configured(factory, Config(), defaultErrorHandler)

  def configured[F[_]: Effect](factory: MetricFactory[F],
                               config: Config,
                               errorHandler: ErrorHandler[F]): Resource[F, Unit] = {
    val F = Effect[F]
    Resource.make(F.delay(makeScheduler(config)))(s => F.delay(s.shutdown())).evalMap { scheduler =>
      F.delay {
        val reporter = new JvmReporter[F](factory)
        scheduler.scheduleWithFixedDelay(runnable(reporter, errorHandler andThen F.toIO),
                                         config.initialDelay.toNanos,
                                         config.delay.toNanos,
                                         TimeUnit.NANOSECONDS)
      }
    }
  }

  private def makeScheduler(config: Config): ScheduledExecutorService =
    Executors.newScheduledThreadPool(1, (r: Runnable) => new Thread(r, config.schedulerThreadName))

  private def runnable[F[_]: Effect](reporter: JvmReporter[F], errorHandler: ErrorHandler[IO]): Runnable =
    () => {
      val syncIO = Effect[F].runAsync(reporter.collect) { res =>
        res.fold(
          e => errorHandler(e),
          IO.pure
        )
      }
      syncIO.unsafeRunSync() // this should not throw exception unless something went horribly wrong
    }

  private def defaultErrorHandler[F[_]: Sync]: ErrorHandler[F] =
    err =>
      Sync[F].delay {
        println(s"Error during metrics collection: ${err.getMessage}")
        err.printStackTrace()
    }
}
