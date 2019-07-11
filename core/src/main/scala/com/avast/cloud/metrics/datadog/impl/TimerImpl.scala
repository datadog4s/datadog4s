package com.avast.cloud.metrics.datadog.impl

import java.util.concurrent.TimeUnit

import cats.effect.{ Clock, Sync }
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.avast.cloud.metrics.datadog.api.{ Tag, Timer }
import com.timgroup.statsd.StatsDClient

import scala.language.higherKinds

class TimerImpl[F[_]: Sync](clock: Clock[F], statsDClient: StatsDClient, aspect: String, sampleRate: Double)
    extends Timer[F] {

  private[this] val F                       = Sync[F]
  private[this] val failedTag: Tag          = Tag.of("success", "false")
  private[this] val succeededTag: Tag       = Tag.of("success", "true")
  private[this] val exceptionTagKey: String = "exception"

  override def time[A](value: F[A], tags: Tag*): F[A] =
    clock.monotonic(TimeUnit.MILLISECONDS).flatMap { start =>
      for {
        a    <- F.recoverWith(value)(measureFailed(start))
        stop <- clock.monotonic(TimeUnit.MILLISECONDS)
        _    <- F.delay(statsDClient.recordExecutionTime(aspect, stop - start, sampleRate, (tags :+ succeededTag): _*))
      } yield {
        a
      }
    }

  private def measureFailed[A](startTime: Long, tags: Tag*): PartialFunction[Throwable, F[A]] = {
    case thr =>
      val computation = for {
        stop    <- clock.monotonic(TimeUnit.MILLISECONDS)
        allTags = tags :+ failedTag :+ Tag.of(exceptionTagKey, thr.getClass.getName)
        _       <- F.delay(statsDClient.recordExecutionTime(aspect, stop - startTime, sampleRate, allTags: _*))
      } yield {
        Unit
      }
      computation >> F.raiseError(thr)
  }

  override def recordExecutionTime(timeInMs: Long, tags: Tag*): F[Unit] = F.delay {
    statsDClient.recordExecutionTime(aspect, timeInMs, sampleRate, tags: _*)
  }
}
