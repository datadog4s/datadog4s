package com.avast.datadog4s.extension.jvm

import cats.effect.kernel.Temporal
import cats.effect.{Resource, Sync}
import com.avast.cloud.datadog4s.helpers.Repeated
import com.avast.datadog4s.api.MetricFactory
import org.typelevel.scalaccompat.annotation.nowarn3

import java.time.Duration

object JvmMonitoring {
  type ErrorHandler[F[_]] = Throwable => F[Unit]

  case class Config(
      delay: Duration = Duration.ofSeconds(60),
      timeout: Duration = Duration.ofSeconds(10)
  )

  @nowarn3 // Context bounds in curlies are incompatible with scala 2.12
  def default[F[_]: Sync: Temporal](factory: MetricFactory[F]): Resource[F, Unit] =
    configured(factory, Config(), defaultErrorHandler)

  @nowarn3 // Context bounds in curlies are incompatible with scala 2.12
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
