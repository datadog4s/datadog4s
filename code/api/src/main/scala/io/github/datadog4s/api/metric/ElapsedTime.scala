package io.github.datadog4s.api.metric

import java.time.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

/** Representation of Elapsed time
  */
trait ElapsedTime[A] { self =>

  /** Return amount of elapsed `timeUnit`s stored in `instance`. For example: given `instance` that represents 24 hours,
    * `amount(instance, TimeUnit.MINUTES)` should return 1440 (24*60)
    */
  def amount(instance: A, timeUnit: TimeUnit): Long
  def contraMap[B](f: B => A): ElapsedTime[B] = (b: B, timeUnit: TimeUnit) => self.amount(f(b), timeUnit)
}

object ElapsedTime {
  def apply[A: ElapsedTime]: ElapsedTime[A] = implicitly[ElapsedTime[A]]

  implicit val durationInstance: ElapsedTime[Duration] = (a: Duration, timeUnit: TimeUnit) =>
    timeUnit match {
      case TimeUnit.NANOSECONDS  => a.toNanos
      case TimeUnit.MICROSECONDS => a.toNanos / 1000
      case TimeUnit.MILLISECONDS => a.toMillis
      case TimeUnit.SECONDS      => a.toMillis / 1000 // jdk1.8 toSeconds returns BigDecimal :(
      case TimeUnit.MINUTES      => a.toMinutes
      case TimeUnit.HOURS        => a.toHours
      case TimeUnit.DAYS         => a.toDays
    }
  implicit val finiteDurationInstance: ElapsedTime[FiniteDuration] =
    ElapsedTime[Duration].contraMap(fd => Duration.ofNanos(fd.toNanos))
}
