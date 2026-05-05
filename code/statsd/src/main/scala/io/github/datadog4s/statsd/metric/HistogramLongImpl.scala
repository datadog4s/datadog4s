package io.github.datadog4s.statsd.metric

import cats.effect.Sync
import com.timgroup.statsd.StatsDClient
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.Histogram
import scala.collection.immutable.Seq

class HistogramLongImpl[F[_]: Sync](
    statsDClient: StatsDClient,
    aspect: String,
    sampleRate: Double,
    defaultTags: Seq[Tag]
) extends Histogram[F, Long] {
  private val F = Sync[F]

  override def record(value: Long, tags: Tag*): F[Unit] =
    F.delay {
      val finalTags = tags ++ defaultTags
      statsDClient.recordHistogramValue(aspect, value, sampleRate, finalTags*)
    }
}
