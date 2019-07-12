package com.avast.cloud.metrics.datadog.noop.metric

import cats.Applicative
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.Histogram

class NoopHistogramLong[F[_]: Applicative] extends Histogram[F, Long] {
  override def record(value: Long, tags: Tag*): F[Unit] = Applicative[F].unit
}
