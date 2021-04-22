package com.avast.datadog4s.api.metric

import com.avast.datadog4s.api.Tag

import java.time.Duration
import java.util.concurrent.TimeUnit

trait Timer[F[_]] {
  def time[A](f: F[A], tags: Tag*): F[A]

  def record(duration: Duration, tags: Tag*): F[Unit] = recordT[Duration](duration, tags: _*)

  /**
   * Record raw measurement represented as long
   * @param duration Measured duration
   * @param timeUnit TimeUnit in which duration is measured
   * @param tags Tags that should be applied for this recording
   */
  def recordRaw(duration: Long, timeUnit: TimeUnit, tags: Tag*): F[Unit] =
    recordT[Duration](Duration.ofNanos(timeUnit.toNanos(duration)), tags: _*)

  /**
   * Record a value that implements [[com.avast.datadog4s.api.metric.AsDuration]] type class. By default we provide instance for [[java.time.Duration]] and [[scala.concurrent.duration.FiniteDuration]].
   *
   * @param value Representation of duration that should be recorded
   * @param tags Tags that should be applied for this recording
   */
  def recordT[T: AsDuration](value: T, tags: Tag*): F[Unit]
}
