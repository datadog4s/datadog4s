package io.github.datadog4s.noop.metric

import cats.Applicative
import io.github.datadog4s.api.metric.{ElapsedTime, Timer}
import io.github.datadog4s.api.Tag

class NoopTimer[F[_]: Applicative] extends Timer[F] {
  override def time[A](f: F[A], tags: Tag*): F[A] = f

  override def record[T: ElapsedTime](duration: T, tags: Tag*): F[Unit] = Applicative[F].unit
}
