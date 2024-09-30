package io.github.datadog4s.api.metric

import io.github.datadog4s.api.Tag
import java.time.Duration
import java.util.concurrent.TimeUnit

trait Timer[F[_]] {
  def time[A](f: F[A], tags: Tag*): F[A]

  /** Record raw measurement represented as long
    * @param elapsed
    *   Measured duration
    * @param timeUnit
    *   TimeUnit in which duration is measured
    * @param tags
    *   Tags that should be applied for this recording
    */
  def recordTime(elapsed: Long, timeUnit: TimeUnit, tags: Tag*): F[Unit] =
    record[Duration](Duration.ofNanos(timeUnit.toNanos(elapsed)), tags*)

  /** Record a value that implements [[ElapsedTime]] type class. By default we provide instance for
    * [[java.time.Duration]] and [[scala.concurrent.duration.FiniteDuration]].
    *
    * @param value
    *   Representation of duration that should be recorded
    * @param tags
    *   Tags that should be applied for this recording
    */
  def record[T: ElapsedTime](value: T, tags: Tag*): F[Unit]
}
