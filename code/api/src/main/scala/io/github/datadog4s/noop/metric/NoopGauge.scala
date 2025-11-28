package io.github.datadog4s.noop.metric

import cats.Applicative
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.Gauge

class NoopGauge[F[_]: Applicative, N] extends Gauge[F, N] {
  override def set(value: N, tags: Tag*): F[Unit] = Applicative[F].unit
}
