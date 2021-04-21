package com.avast.datadog4s.api.metric

import java.time.Duration
import scala.concurrent.duration.FiniteDuration

trait AsDuration[A] { self =>
  def toMillis(a: A): Long
  def contraMap[B](f: B => A): AsDuration[B] = (b: B) => self.toMillis(f(b))
}

object AsDuration {
  def apply[A: AsDuration]: AsDuration[A] = implicitly[AsDuration[A]]

  implicit val longInstance: AsDuration[Long]                     = (a: Long) => a
  implicit val durationInstance: AsDuration[Duration]             = AsDuration[Long].contraMap(_.toMillis)
  implicit val finiteDurationInstance: AsDuration[FiniteDuration] = AsDuration[Long].contraMap(_.toMillis)
}
