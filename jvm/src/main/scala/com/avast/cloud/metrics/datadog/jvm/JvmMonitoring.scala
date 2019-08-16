package com.avast.cloud.metrics.datadog.jvm

import java.util.concurrent.{ Executors, TimeUnit }

import cats.effect.{ Effect, IO, Resource, Sync }
import com.avast.cloud.metrics.datadog.api.MetricFactory

import scala.util.Try

object JvmMonitoring {
  def make[F[_]: Effect](factory: MetricFactory[F]): Resource[F, Unit] = {
    val F = Effect[F]
    Resource.make(F.delay(Executors.newScheduledThreadPool(1)))(s => F.delay(s.shutdown())).evalMap { scheduler =>
      F.delay {
        val reporter = new JvmReporter[F](factory)
        scheduler.scheduleWithFixedDelay(runnable(reporter), 0, 1, TimeUnit.MINUTES)
      }
    }
  }

  private def runnable[F[_]: Effect](reporter: JvmReporter[F]): Runnable = new Runnable {
    override def run(): Unit =
      Try(Effect[F].toIO(reporter.collect).unsafeRunSync()).failed.foreach { err =>
        println(s"Error during metrics collection: ${err.getMessage}")
        err.printStackTrace()
      }
  }
}
