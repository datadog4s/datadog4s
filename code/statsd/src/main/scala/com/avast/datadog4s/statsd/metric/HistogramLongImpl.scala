package com.avast.datadog4s.statsd.metric

import cats.effect.Sync
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Histogram
import com.timgroup.statsd.StatsDClient
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
      statsDClient.recordHistogramValue(aspect, value, sampleRate, (tags ++ defaultTags)*)
    }
}
