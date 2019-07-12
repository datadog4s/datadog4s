package com.avast.cloud.metrics.datadog.statsd.metric

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.Histogram
import com.timgroup.statsd.StatsDClient

class HistogramDoubleImpl[F[_]: Sync](statsDClient: StatsDClient, aspect: String, sampleRate: Double, defaultTags: Vector[Tag])
    extends Histogram[F, Double] {

  private[this] val F = Sync[F]

  override def record(value: Double, tags: Tag*): F[Unit] = F.delay {
    statsDClient.recordHistogramValue(aspect, value, sampleRate, (tags ++ defaultTags): _*)
  }

}
