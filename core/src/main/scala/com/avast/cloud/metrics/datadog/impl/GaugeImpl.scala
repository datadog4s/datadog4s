package com.avast.cloud.metrics.datadog.impl

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.{ Gauge, Tag }

import scala.language.higherKinds

class GaugeImpl[F[_]: Sync] extends Gauge[F] {
  override def set(value: Long, tags: Tag*): F[Unit] = ???

  override def set(value: Double, tags: Tag*): F[Unit] = ???
}
