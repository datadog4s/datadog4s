package com.avast.cloud.metrics.datadog.noop

import cats.Monad
import com.avast.cloud.metrics.datadog.api.{Histogram, Tag}

import scala.language.higherKinds

class NoopHistogram[F[_]: Monad] extends Histogram[F]{
  override def record(value: Long, tags: Tag*): F[Unit] = Monad[F].unit

  override def record(value: Double, tags: Tag*): F[Unit] = Monad[F].unit
}
