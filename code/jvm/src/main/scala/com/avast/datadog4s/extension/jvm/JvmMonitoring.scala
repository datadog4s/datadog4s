package com.avast.datadog4s.extension.jvm

import java.time.Duration

import cats.effect.{ ConcurrentEffect, Resource, Sync, Timer }
import com.avast.cloud.datadog4s.common.Repeated
import com.avast.datadog4s.api.MetricFactory

object JvmMonitoring {
  type ErrorHandler[F[_]] = Throwable => F[Unit]

  case class Config(
    initialDelay: Duration = Duration.ofMillis(0),
    delay: Duration = Duration.ofSeconds(60),
    timeout: Duration = Duration.ofSeconds(10),
  )

  def default[F[_]: ConcurrentEffect: Timer](factory: MetricFactory[F]): Resource[F, Unit] =
    configured(factory, Config(), defaultErrorHandler)

  def configured[F[_]: ConcurrentEffect: Timer](
    factory: MetricFactory[F],
    config: Config,
    errorHandler: ErrorHandler[F]
  ): Resource[F, Unit] = {
    val reporter = new JvmReporter[F](factory)

    Repeated.run[F](config.initialDelay, config.delay, config.timeout, errorHandler)(reporter.collect).map(_ => ())
  }

  private def defaultErrorHandler[F[_]: Sync]: ErrorHandler[F] =
    err =>
      Sync[F].delay {
        println(s"Error during metrics collection: ${err.getMessage}")
        err.printStackTrace()
      }
}
