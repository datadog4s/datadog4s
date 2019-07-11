package com.avast.cloud.metrics.datadog.noop

import cats.Monad
import com.avast.cloud.metrics.datadog.api.{ Tag, Timer }

import scala.language.higherKinds

class NoopTimer[F[_]: Monad] extends Timer[F] {
  override def time[A](f: F[A], tags: Tag*): F[A] = f

  override def recordExecutionTime(timeInMs: Long, tags: Tag*): F[Unit] = Monad[F].unit
}
