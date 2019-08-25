package com.avast.datadog4s.noop.metric

import cats.Applicative
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Gauge

class NoopGaugeDouble[F[_]: Applicative] extends Gauge[F, Double] {
  override def set(value: Double, tags: Tag*): F[Unit] = Applicative[F].unit
}
