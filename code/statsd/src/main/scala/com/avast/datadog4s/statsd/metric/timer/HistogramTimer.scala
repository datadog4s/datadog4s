package com.avast.datadog4s.statsd.metric.timer

import cats.effect.{Clock, Sync}
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.ElapsedTime
import com.avast.datadog4s.statsd.metric.TimerImpl
import com.timgroup.statsd.StatsDClient

import scala.concurrent.duration.TimeUnit

class HistogramTimer[F[_]: Sync](
    clock: Clock[F],
    statsDClient: StatsDClient,
    aspect: String,
    sampleRate: Double,
    defaultTags: Seq[Tag],
    timeUnit: TimeUnit
) extends TimerImpl[F](clock) {
  override def record[T: ElapsedTime](t: T, tags: Tag*): F[Unit] =
    Sync[F].delay {
      val finalTags = tags ++ defaultTags
      statsDClient.recordHistogramValue(
        aspect,
        ElapsedTime[T].amount(t, timeUnit),
        sampleRate,
        finalTags *
      )
    }

}
