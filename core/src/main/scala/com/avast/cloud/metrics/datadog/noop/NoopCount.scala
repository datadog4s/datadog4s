package com.avast.cloud.metrics.datadog.noop

import cats.Monad
import com.avast.cloud.metrics.datadog.api.{ Count, Tag }

import scala.language.higherKinds

class NoopCount[F[_]: Monad] extends Count[F] {
  override def inc(tags: Tag*): F[Unit] = Monad[F].unit

  override def modify(delta: Int, tags: Tag*): F[Unit] = Monad[F].unit

  override def dec(tags: Tag*): F[Unit] = Monad[F].unit
}
