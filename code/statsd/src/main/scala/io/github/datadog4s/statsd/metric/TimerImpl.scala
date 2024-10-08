package io.github.datadog4s.statsd.metric

import cats.effect.{Clock, Sync}
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.Timer
import io.github.datadog4s.api.tag.Tagger

import scala.concurrent.duration.FiniteDuration

abstract class TimerImpl[F[_]: Sync](
    clock: Clock[F]
) extends Timer[F] {
  private val successTagger: Tagger[Boolean]     = Tagger.make("success")
  private val failedTag: Tag                     = successTagger.tag(false)
  private val succeededTag: Tag                  = successTagger.tag(true)
  private val exceptionTagger: Tagger[Throwable] = Tagger.make("exception")
  private val F                                  = Sync[F]

  override def time[A](value: F[A], tags: Tag*): F[A] =
    for {
      start <- clock.monotonic
      a     <- F.recoverWith(value)(measureFailed(start))
      stop  <- clock.monotonic
      finalTags = tags :+ succeededTag
      _ <- record(stop.minus(start), finalTags*)
    } yield a

  private def measureFailed[A](startTime: FiniteDuration, tags: Tag*): PartialFunction[Throwable, F[A]] = {
    case thr: Throwable =>
      val finalTags = tags :+ exceptionTagger.tag(thr) :+ failedTag
      val computation = for {
        stop <- clock.monotonic
        _    <- record(stop.minus(startTime), finalTags*)
      } yield ()
      computation >> F.raiseError[A](thr)
  }
}
