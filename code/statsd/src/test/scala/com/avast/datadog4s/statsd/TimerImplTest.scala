package com.avast.datadog4s.statsd

import cats.effect.{ Clock, IO }
import com.avast.datadog4s.api.MetricFactory.TimerMode
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.statsd.metric.TimerImpl

import scala.jdk.CollectionConverters._

class TimerImplTest extends munit.FunSuite {

  trait TestFixture {
    val aspect: String
    val sampleRate: Double

    val statsD: JMockStatsDClient
    val clock: Clock[IO]
    val timer: TimerImpl[IO]
  }

  private val fixture = FunFixture[TestFixture](
    setup = { _ =>
      new TestFixture {
        val aspect: String = "metric"
        val sampleRate     = 1.0

        val statsD: JMockStatsDClient = MockStatsDClient()
        val clock: Clock[IO]          = new MockClock
        val timer                     = new TimerImpl[IO](clock, statsD, aspect, sampleRate, Vector.empty, TimerMode.HistogramTimer)
      }
    },
    _ => ()
  )

  fixture.test("time F[A] should report success with label success:true") { f =>
    val res = f.timer.time(IO.delay("hello world")).unsafeRunSync()

    assert(
      f.statsD.getHistory.get.asScala.toVector == Vector(
        new ExecutionTimeRecord(f.aspect, 20, f.sampleRate, Vector(Tag.of("success", "true")).asJava)
      )
    )
    assert(res == "hello world")
  }

  fixture.test("time F[A] should report failure with label failure:true and exception name") { f =>
    val res = f.timer.time(IO.raiseError(new NoSuchElementException("fail")))

    intercept[NoSuchElementException](res.unsafeRunSync())
    assert(
      f.statsD.getHistory.get().asScala.toVector == Vector(
        new ExecutionTimeRecord(
          f.aspect,
          20,
          f.sampleRate,
          Vector(Tag.of("exception", "java.util.NoSuchElementException"), Tag.of("success", "false")).asJava
        )
      )
    )
  }

}
