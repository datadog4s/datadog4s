package io.github.datadog4s.noop.metric

import cats.Applicative
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.Histogram

class NoopHistogram[F[_]: Applicative, N] extends Histogram[F, N] {
  override def record(value: N, tags: Tag*): F[Unit] = Applicative[F].unit
}
