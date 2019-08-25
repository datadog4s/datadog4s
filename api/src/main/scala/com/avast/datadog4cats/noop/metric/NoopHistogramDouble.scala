package com.avast.datadog4cats.noop.metric

import cats.Applicative
import com.avast.datadog4cats.api.Tag
import com.avast.datadog4cats.api.metric.Histogram

class NoopHistogramDouble[F[_]: Applicative] extends Histogram[F, Double] {
  override def record(value: Double, tags: Tag*): F[Unit] = Applicative[F].unit
}
