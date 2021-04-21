package com.avast.datadog4s.api.metric

import com.avast.datadog4s.api.Tag

import java.time.Duration

trait Timer[F[_]] {
  def time[A](f: F[A], tags: Tag*): F[A]

  def record(duration: Duration, tags: Tag*): F[Unit] = recordT[Duration](duration, tags: _*)

  /**
   * Record a value that implements [[AsDuration]] type class.
   */
  def recordT[T: AsDuration](value: T, tags: Tag*): F[Unit] = recordMillis(AsDuration[T].toMillis(value), tags: _*)
  def recordMillis(long: Long, tags: Tag*): F[Unit]
}
