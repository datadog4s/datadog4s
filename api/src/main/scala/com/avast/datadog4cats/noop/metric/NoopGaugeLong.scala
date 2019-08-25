package com.avast.datadog4cats.noop.metric

import cats.Applicative
import com.avast.datadog4cats.api.Tag
import com.avast.datadog4cats.api.metric.Gauge

class NoopGaugeLong[F[_]: Applicative] extends Gauge[F, Long] {
  override def set(value: Long, tags: Tag*): F[Unit] = Applicative[F].unit
}
