package com.avast.cloud.metrics.datadog.noop

import cats.Monad
import com.avast.cloud.metrics.datadog.api.{Tag, UniqueSet}

import scala.language.higherKinds

class NoopUniqueSet[F[_]: Monad] extends UniqueSet[F] {
  override def record(value: String, tags: Tag*): F[Unit] = Monad[F].unit
}
