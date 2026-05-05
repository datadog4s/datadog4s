package io.github.datadog4s.noop.metric

import cats.Applicative
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.UniqueSet

class NoopUniqueSet[F[_]: Applicative] extends UniqueSet[F] {
  override def record(value: String, tags: Tag*): F[Unit] = Applicative[F].unit
}
