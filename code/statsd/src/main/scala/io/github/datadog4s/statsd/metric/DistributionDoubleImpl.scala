package io.github.datadog4s.statsd.metric

import cats.effect.Sync
import com.timgroup.statsd.StatsDClient
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.Distribution

class DistributionDoubleImpl[F[_]: Sync](
    statsDClient: StatsDClient,
    aspect: String,
    sampleRate: Double,
    defaultTags: Seq[Tag]
) extends Distribution[F, Double] {
  private val F = Sync[F]

  override def record(value: Double, tags: Tag*): F[Unit] =
    F.delay {
      val finalTags = tags ++ defaultTags
      statsDClient.recordDistributionValue(aspect, value, sampleRate, finalTags*)
    }
}
