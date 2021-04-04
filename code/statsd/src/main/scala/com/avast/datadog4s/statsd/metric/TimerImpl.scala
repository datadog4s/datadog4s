package com.avast.datadog4s.statsd.metric

import cats.effect.{ Clock, Sync }
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Timer
import com.avast.datadog4s.api.tag.Tagger

import java.util.concurrent.TimeUnit

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
      start <- clock.monotonic(TimeUnit.NANOSECONDS)
      a     <- F.recoverWith(value)(measureFailed(start))
      stop  <- clock.monotonic(TimeUnit.NANOSECONDS)
      _     <- recordMillis(nanoToMilli(stop - start), (tags :+ succeededTag): _*)
    } yield a

  private def measureFailed[A](startTime: Long, tags: Tag*): PartialFunction[Throwable, F[A]] = { case thr: Throwable =>
    val finalTags   = tags :+ exceptionTagger.tag(thr) :+ failedTag
    val computation = for {
      stop <- clock.monotonic(TimeUnit.NANOSECONDS)
      _    <- recordMillis(nanoToMilli(stop - startTime), finalTags: _*)
    } yield ()
    computation >> F.raiseError[A](thr)
  }

  private def nanoToMilli(nano: Long): Long =
    nano / 1000 / 1000

}
