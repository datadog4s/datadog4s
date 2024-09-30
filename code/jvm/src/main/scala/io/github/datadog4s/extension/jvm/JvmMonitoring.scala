package io.github.datadog4s.extension.jvm

import cats.effect.kernel.Temporal
import cats.effect.{Resource, Sync}
import io.github.datadog4s.api.MetricFactory
import io.github.datadog4s.helpers.Repeated

import java.time.Duration

object JvmMonitoring {
  type ErrorHandler[F[_]] = Throwable => F[Unit]

  case class Config(
      delay: Duration = Duration.ofSeconds(60),
      timeout: Duration = Duration.ofSeconds(10)
  )

  def default[F[_]: Sync: Temporal](factory: MetricFactory[F]): Resource[F, Unit] =
    configured(factory, Config(), defaultErrorHandler)

  def configured[F[_]: Sync: Temporal](
      factory: MetricFactory[F],
      config: Config,
      errorHandler: ErrorHandler[F]
  ): Resource[F, Unit] = {
    val reporter = new JvmReporter[F](factory)

    Repeated.run[F](config.delay, config.timeout, errorHandler)(reporter.collect).map(_ => ())
  }

  private def defaultErrorHandler[F[_]: Sync]: ErrorHandler[F] =
    err =>
      Sync[F].delay {
        println(s"Error during metrics collection: ${err.getMessage}")
        err.printStackTrace()
      }
}
