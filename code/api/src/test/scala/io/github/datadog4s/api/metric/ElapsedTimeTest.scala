package io.github.datadog4s.api.metric

import java.time.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class ElapsedTimeTest extends munit.FunSuite {
  test("Duration should return millis correctly") {
    val duration = Duration.ofHours(1)
    assertEquals(ElapsedTime[Duration].amount(duration, TimeUnit.MILLISECONDS), 60L * 60 * 1000)
    assertEquals(ElapsedTime[Duration].amount(duration, TimeUnit.HOURS), 1L)
    assertEquals(ElapsedTime[Duration].amount(duration, TimeUnit.MINUTES), 60L)
    assertEquals(ElapsedTime[Duration].amount(duration, TimeUnit.NANOSECONDS), 60L * 60 * 1000 * 1000 * 1000)
  }

  test("FiniteDuration should return millis correctly") {
    val duration = FiniteDuration(1, TimeUnit.HOURS)
    assertEquals(ElapsedTime[FiniteDuration].amount(duration, TimeUnit.MILLISECONDS), 60L * 60 * 1000)
    assertEquals(ElapsedTime[FiniteDuration].amount(duration, TimeUnit.HOURS), 1L)
    assertEquals(ElapsedTime[FiniteDuration].amount(duration, TimeUnit.MINUTES), 60L)
    assertEquals(
      ElapsedTime[FiniteDuration].amount(duration, TimeUnit.NANOSECONDS),
      60L * 60 * 1000 * 1000 * 1000
    )
  }

}
