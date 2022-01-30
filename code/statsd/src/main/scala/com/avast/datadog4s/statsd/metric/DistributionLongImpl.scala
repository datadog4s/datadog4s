package com.avast.datadog4s.statsd.metric

import cats.effect.Sync
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Distribution
import com.timgroup.statsd.StatsDClient

class DistributionLongImpl[F[_]: Sync](
    statsDClient: StatsDClient,
    aspect: String,
    sampleRate: Double,
    defaultTags: Seq[Tag]
) extends Distribution[F, Long] {
  private val F = Sync[F]
  override def record(value: Long, tags: Tag*): F[Unit] =
    F.delay {
      statsDClient.recordDistributionValue(aspect, value, sampleRate, (tags ++ defaultTags)*)
    }
}
