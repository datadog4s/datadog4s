package com.avast.datadog4s.api.metric

import java.time.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class JvmMonitoringTest extends munit.FunSuite {
  test("Long should propagate return the value") {
    val value = 1337L
    assertEquals(AsDuration[Long].toMillis(1337), value)
  }

  test("Duration should return millis correctly") {
    val duration = Duration.ofHours(1)
    assertEquals(AsDuration[Duration].toMillis(duration), 60L * 60 * 1000)
  }

  test("FiniteDuration should return millis correctly") {
    val duration = FiniteDuration(1, TimeUnit.HOURS)
    assertEquals(AsDuration[FiniteDuration].toMillis(duration), 60L * 60 * 1000)
  }


}
