package com.avast.datadog4s.api.metric

import java.time.Duration
import scala.concurrent.duration.FiniteDuration

import com.avast.datadog4s.api.Tag

trait Timer[F[_]] {
  def time[A](f: F[A], tags: Tag*): F[A]

  def record(duration: Duration, tags: Tag*): F[Unit]       = recordMillis(duration.toMillis, tags: _*)
  def record(duration: FiniteDuration, tags: Tag*): F[Unit] = recordMillis(duration.toMillis, tags: _*)
  def recordMillis(long: Long, tags: Tag*): F[Unit]
}
