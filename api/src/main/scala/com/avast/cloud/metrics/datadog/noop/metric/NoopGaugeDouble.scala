package com.avast.cloud.metrics.datadog.noop.metric

import cats.Applicative
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.Gauge

class NoopGaugeDouble[F[_]: Applicative] extends Gauge[F, Double] {
  override def set(value: Double, tags: Tag*): F[Unit] = Applicative[F].unit
}
