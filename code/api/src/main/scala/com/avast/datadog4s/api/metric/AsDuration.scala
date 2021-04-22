package com.avast.datadog4s.api.metric

import java.time.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

trait AsDuration[A] { self =>
  def valueOfTimeUnit(a: A, timeUnit: TimeUnit): Long
  def contraMap[B](f: B => A): AsDuration[B] = (b: B, timeUnit: TimeUnit) => self.valueOfTimeUnit(f(b), timeUnit)
}

object AsDuration {
  def apply[A: AsDuration]: AsDuration[A] = implicitly[AsDuration[A]]

  implicit val durationInstance: AsDuration[Duration]             = (a: Duration, timeUnit: TimeUnit) =>
    timeUnit match {
      case TimeUnit.NANOSECONDS  => a.toNanos
      case TimeUnit.MICROSECONDS => a.toNanos / 1000
      case TimeUnit.MILLISECONDS => a.toMillis
      case TimeUnit.SECONDS      => a.toMillis / 1000 // jdk1.8 toSeconds returns BigDecimal :(
      case TimeUnit.MINUTES      => a.toMinutes
      case TimeUnit.HOURS        => a.toHours
      case TimeUnit.DAYS         => a.toDays
    }
  implicit val finiteDurationInstance: AsDuration[FiniteDuration] =
    AsDuration[Duration].contraMap(fd => Duration.ofNanos(fd.toNanos))
}
