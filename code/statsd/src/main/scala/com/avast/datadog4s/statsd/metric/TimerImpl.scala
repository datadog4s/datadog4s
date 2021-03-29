package com.avast.datadog4s.statsd.metric

import cats.effect.{Clock, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Timer
import com.avast.datadog4s.api.tag.Tagger

import java.time.Duration
import scala.concurrent.duration.FiniteDuration

abstract class TimerImpl[F[_]: Sync](
  clock: Clock[F]
) extends Timer[F] {
  private[this] val F                                  = Sync[F]
  private[this] val successTagger: Tagger[Boolean]     = Tagger.make("success")
  private[this] val failedTag: Tag                     = successTagger.tag(false)
  private[this] val succeededTag: Tag                  = successTagger.tag(true)
  private[this] val exceptionTagger: Tagger[Throwable] = Tagger.make("exception")

  override def time[A](value: F[A], tags: Tag*): F[A] =
    for {
      start <- clock.monotonic
      a     <- F.recoverWith(value)(measureFailed(start))
      stop  <- clock.monotonic
      _     <- record(Duration.ofNanos(stop.minus(start).toNanos), (tags :+ succeededTag): _*)
    } yield a

  private def measureFailed[A](startTime: FiniteDuration, tags: Tag*): PartialFunction[Throwable, F[A]] = { case thr: Throwable =>
    val finalTags   = tags :+ exceptionTagger.tag(thr) :+ failedTag
    val computation = for {
      stop <- clock.monotonic
      _    <- record(Duration.ofNanos(stop.minus(startTime).toNanos), finalTags: _*)
    } yield ()
    computation >> F.raiseError[A](thr)
  }

}
