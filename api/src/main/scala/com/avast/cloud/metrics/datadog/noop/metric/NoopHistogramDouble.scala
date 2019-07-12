package com.avast.cloud.metrics.datadog.noop.metric

import cats.Applicative
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.Histogram

class NoopHistogramDouble[F[_]: Applicative] extends Histogram[F, Double] {
  override def record(value: Double, tags: Tag*): F[Unit] = Applicative[F].unit
}
