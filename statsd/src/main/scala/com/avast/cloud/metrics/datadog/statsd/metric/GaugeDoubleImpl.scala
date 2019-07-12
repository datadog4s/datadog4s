package com.avast.cloud.metrics.datadog.statsd.metric

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.Gauge
import com.timgroup.statsd.StatsDClient

class GaugeDoubleImpl[F[_]: Sync](statsDClient: StatsDClient, aspect: String, sampleRate: Double, defaultTags: Vector[Tag])
    extends Gauge[F, Double] {
  private[this] val F = Sync[F]

  override def set(value: Double, tags: Tag*): F[Unit] = F.delay {
    statsDClient.recordGaugeValue(aspect, sampleRate, value, (tags ++ defaultTags): _*)
  }
}
