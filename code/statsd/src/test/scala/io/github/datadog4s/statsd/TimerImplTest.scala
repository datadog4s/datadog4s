package io.github.datadog4s.statsd

import cats.effect.{Clock, IO}
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.Timer
import io.github.datadog4s.statsd.metric.timer.HistogramTimer

import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*

class TimerImplTest extends munit.FunSuite {

  trait TestFixture {
    val aspect: String
    val sampleRate: Double

    val statsD: JMockStatsDClient
    val clock: Clock[IO]
    val timer: Timer[IO]
  }

  private val fixture = FunFixture[TestFixture](
    setup = { _ =>
      new TestFixture {
        val aspect: String = "metric"
        val sampleRate     = 1.0

        val statsD: JMockStatsDClient = MockStatsDClient()
        val clock: Clock[IO]          = new MockClock
        val timer =
          new HistogramTimer[IO](clock, statsD, aspect, sampleRate, Vector.empty, TimeUnit.MILLISECONDS): Timer[IO]
      }
    },
    _ => ()
  )

  fixture.test("time F[A] should report success with label success:true") { f =>
    val res = f.timer.time(IO.delay("hello world")).unsafeRunSync()

    assertEquals(
      f.statsD.getHistory.get.asScala.toVector,
      Vector(
        new HistogramRecord(f.aspect, 20, f.sampleRate, Vector(Tag.of("success", "true")).asJava)
      )
    )
    assertEquals(res, "hello world")
  }

  fixture.test("time F[A] should report failure with label failure:true and exception name") { f =>
    val res = f.timer.time(IO.raiseError(new NoSuchElementException("fail")))

    val _ = intercept[NoSuchElementException](res.unsafeRunSync())
    assertEquals(
      f.statsD.getHistory.get().asScala.toVector,
      Vector(
        new HistogramRecord(
          f.aspect,
          20,
          f.sampleRate,
          Vector(Tag.of("exception", "java.util.NoSuchElementException"), Tag.of("success", "false")).asJava
        )
      )
    )
  }

}
