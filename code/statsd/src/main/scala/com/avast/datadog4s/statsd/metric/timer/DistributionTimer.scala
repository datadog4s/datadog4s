package com.avast.datadog4s.statsd.metric.timer

import cats.effect.{ Clock, Sync }
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.statsd.metric.TimerImpl
import com.timgroup.statsd.StatsDClient

import java.time.Duration

class DistributionTimer[F[_]: Sync](
  clock: Clock[F],
  statsDClient: StatsDClient,
  aspect: String,
  sampleRate: Double,
  defaultTags: Seq[Tag]
) extends TimerImpl[F](clock) {
  override def record(duration: Duration, tags: Tag*): F[Unit] = Sync[F].delay {
    statsDClient.recordDistributionValue(aspect, duration.toMillis, sampleRate, (tags ++ defaultTags): _*)
  }
}
