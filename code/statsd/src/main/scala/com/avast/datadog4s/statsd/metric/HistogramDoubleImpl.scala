package com.avast.datadog4s.statsd.metric

import cats.effect.Sync
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Histogram
import com.timgroup.statsd.StatsDClient
import scala.collection.immutable.Seq

class HistogramDoubleImpl[F[_]: Sync](
  statsDClient: StatsDClient,
  aspect: String,
  sampleRate: Double,
  defaultTags: Seq[Tag]
) extends Histogram[F, Double] {
  private[this] val F = Sync[F]

  override def record(value: Double, tags: Tag*): F[Unit] =
    F.delay {
      statsDClient.recordHistogramValue(aspect, value, sampleRate, (tags ++ defaultTags): _*)
    }
}
