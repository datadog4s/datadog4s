package com.avast.datadog4s.noop.metric

import java.time.Duration

import cats.Applicative
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Timer

class NoopTimer[F[_]: Applicative] extends Timer[F] {
  override def time[A](f: F[A], tags: Tag*): F[A] = f

  override def record(duration: Duration, tags: Tag*): F[Unit] = Applicative[F].unit
}
