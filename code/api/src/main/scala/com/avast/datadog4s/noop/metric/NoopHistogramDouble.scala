package com.avast.datadog4s.noop.metric

import cats.Applicative
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Histogram

class NoopHistogramDouble[F[_]: Applicative] extends Histogram[F, Double] {
  override def record(value: Double, tags: Tag*): F[Unit] = Applicative[F].unit
}
