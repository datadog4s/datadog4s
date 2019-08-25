package com.avast.datadog4s.noop.metric

import cats.Applicative
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Histogram

class NoopHistogramLong[F[_]: Applicative] extends Histogram[F, Long] {
  override def record(value: Long, tags: Tag*): F[Unit] = Applicative[F].unit
}
