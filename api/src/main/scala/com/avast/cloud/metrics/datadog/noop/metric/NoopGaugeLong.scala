package com.avast.cloud.metrics.datadog.noop.metric

import cats.Applicative
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.Gauge

class NoopGaugeLong[F[_]: Applicative] extends Gauge[F, Long] {
  override def set(value: Long, tags: Tag*): F[Unit] = Applicative[F].unit
}
