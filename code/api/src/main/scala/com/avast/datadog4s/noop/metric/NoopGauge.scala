package com.avast.datadog4s.noop.metric

import cats.Applicative
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Gauge

class NoopGauge[F[_]: Applicative, N] extends Gauge[F, N] {
  override def set(value: N, tags: Tag*): F[Unit] = Applicative[F].unit
}
