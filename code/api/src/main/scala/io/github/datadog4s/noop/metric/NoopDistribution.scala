package io.github.datadog4s.noop.metric

import cats.Applicative
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.Distribution

class NoopDistribution[F[_]: Applicative, N] extends Distribution[F, N] {
  override def record(value: N, tags: Tag*): F[Unit] = Applicative[F].unit
}
