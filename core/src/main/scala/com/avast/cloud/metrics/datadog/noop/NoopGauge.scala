package com.avast.cloud.metrics.datadog.noop

import cats.Monad
import com.avast.cloud.metrics.datadog.api.{ Gauge, Tag }

import scala.language.higherKinds

class NoopGauge[F[_]: Monad] extends Gauge[F] {
  override def set(value: Long, tags: Tag*): F[Unit] = Monad[F].unit

  override def set(value: Double, tags: Tag*): F[Unit] = Monad[F].unit
}
