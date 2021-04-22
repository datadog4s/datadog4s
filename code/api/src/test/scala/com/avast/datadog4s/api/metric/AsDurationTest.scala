package com.avast.datadog4s.api.metric

import java.time.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class AsDurationTest extends munit.FunSuite {
  test("Duration should return millis correctly") {
    val duration = Duration.ofHours(1)
    assertEquals(AsDuration[Duration].valueOfTimeUnit(duration, TimeUnit.MILLISECONDS), 60L * 60 * 1000)
    assertEquals(AsDuration[Duration].valueOfTimeUnit(duration, TimeUnit.HOURS), 1L)
    assertEquals(AsDuration[Duration].valueOfTimeUnit(duration, TimeUnit.MINUTES), 60L)
    assertEquals(AsDuration[Duration].valueOfTimeUnit(duration, TimeUnit.NANOSECONDS), 60L * 60 * 1000 * 1000 * 1000)
  }

  test("FiniteDuration should return millis correctly") {
    val duration = FiniteDuration(1, TimeUnit.HOURS)
    assertEquals(AsDuration[FiniteDuration].valueOfTimeUnit(duration, TimeUnit.MILLISECONDS), 60L * 60 * 1000)
    assertEquals(AsDuration[FiniteDuration].valueOfTimeUnit(duration, TimeUnit.HOURS), 1L)
    assertEquals(AsDuration[FiniteDuration].valueOfTimeUnit(duration, TimeUnit.MINUTES), 60L)
    assertEquals(AsDuration[FiniteDuration].valueOfTimeUnit(duration, TimeUnit.NANOSECONDS), 60L * 60 * 1000 * 1000 * 1000)
  }

}
