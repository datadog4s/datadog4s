package com.avast.datadog4cats.noop.metric

import cats.Applicative
import com.avast.datadog4cats.api.Tag
import com.avast.datadog4cats.api.metric.UniqueSet

class NoopUniqueSet[F[_]: Applicative] extends UniqueSet[F] {
  override def record(value: String, tags: Tag*): F[Unit] = Applicative[F].unit
}
