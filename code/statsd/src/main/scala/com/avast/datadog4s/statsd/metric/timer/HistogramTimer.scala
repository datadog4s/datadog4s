package com.avast.datadog4s.statsd.metric.timer

import cats.effect.{ Clock, Sync }
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.AsDuration
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
  override def recordT[T: AsDuration](t: T, tags: Tag*): F[Unit] =
    Sync[F].delay {
      statsDClient.recordHistogramValue(
        aspect,
        AsDuration[T].valueOfTimeUnit(t, timeUnit),
        sampleRate,
        (tags ++ defaultTags): _*
      )
    }

}
