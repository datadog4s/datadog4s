package com.avast.datadog4s.extension.jvm

import java.time.Duration
import java.util.concurrent.{ ScheduledExecutorService, ScheduledThreadPoolExecutor, ThreadFactory, TimeUnit }

import cats.effect.{ Effect, IO, Resource, Sync }
import com.avast.datadog4s.api.MetricFactory

object JvmMonitoring {
  type ErrorHandler[F[_]] = Throwable => F[Unit]

  case class Config(
    initialDelay: Duration = Duration.ofMillis(0),
    delay: Duration = Duration.ofMinutes(1),
    schedulerThreadName: String = "datadog4s-jvm-reporter"
  )

  def default[F[_]: Effect](factory: MetricFactory[F]): Resource[F, Unit] =
    configured(factory, Config(), defaultErrorHandler)

  def configured[F[_]: Effect](
    factory: MetricFactory[F],
    config: Config,
    errorHandler: ErrorHandler[F]
  ): Resource[F, Unit] = {
    val F = Effect[F]
    Resource.make(F.delay(makeScheduler(config)))(s => F.delay(s.shutdown())).evalMap { scheduler =>
      F.delay {
        val reporter = new JvmReporter[F](factory)
        val _ = scheduler.scheduleWithFixedDelay(
          runnable(F.toIO(reporter.collect), errorHandler andThen F.toIO),
          config.initialDelay.toNanos,
          config.delay.toNanos,
          TimeUnit.NANOSECONDS
        )
      }
    }
  }

  private def makeScheduler(config: Config): ScheduledExecutorService = {
    val threadFactory: ThreadFactory = (r: Runnable) => {
      val thread = new Thread(r)
      thread.setName(s"${config.schedulerThreadName}-${thread.getId}")
      thread.setDaemon(true)
      thread
    }

    val executor = new ScheduledThreadPoolExecutor(1, threadFactory)
    executor.setRemoveOnCancelPolicy(true)
    executor
  }

  private def runnable(reportMetrics: IO[Unit], errorHandler: ErrorHandler[IO]): Runnable =
    () => reportMetrics.handleErrorWith(errorHandler).unsafeRunSync()

  private def defaultErrorHandler[F[_]: Sync]: ErrorHandler[F] =
    err =>
      Sync[F].delay {
        println(s"Error during metrics collection: ${err.getMessage}")
        err.printStackTrace()
      }
}
