package com.avast.datadog4s.statsd

import java.util.concurrent.TimeUnit

import cats.effect.concurrent.Ref
import cats.effect.{Clock, IO}
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.statsd.MockStatsDClient.ExecutionTimeRecord
import com.avast.datadog4s.statsd.metric.TimerImpl
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.{Assertions, BeforeAndAfter}

class TimerImplTest extends AnyFlatSpec with BeforeAndAfter with Assertions {

  import cats.implicits.catsSyntaxFlatMapOps

  trait Fixtures {
    val aspect: String = "metric"
    val sampleRate     = 1.0

    val statsD: MockStatsDClient = MockStatsDClient()
    val clock: Clock[IO]      = new Clock[IO] {
      val callCount = Ref.of[IO, Int](0).unsafeRunSync()
      override def realTime(unit: TimeUnit): IO[Long] = ???

      override def monotonic(unit: TimeUnit): IO[Long] = {
        callCount.get.flatMap { count =>
          if (count == 0) {
            callCount.update(_ + 1) >> IO.pure(10L * 1000 * 1000)
          } else {
            IO.pure(30L * 1000 * 1000)
          }
        }
      }
    }

    val timer = new TimerImpl[IO](clock, statsD, aspect, sampleRate, Vector.empty)
  }

  "time F[A]" should "report success with label success:true" in new Fixtures {
    private val res = timer.time(IO.delay("hello world")).unsafeRunSync()

    assertResult(statsD.history.get){Vector(ExecutionTimeRecord(aspect, 20, sampleRate, Vector(Tag.of("success", "true"))))}
    assertResult(res)("hello world")
  }

  it should "report failure with label failure:true and exception name" in new Fixtures {
    private val res = timer.time(IO.raiseError(new NoSuchElementException("fail")))

    assertThrows[NoSuchElementException](res.unsafeRunSync())
    assertResult(statsD.history.get()){Vector(ExecutionTimeRecord(
        aspect,
        20,
        sampleRate,
        Vector(Tag.of("exception", "java.util.NoSuchElementException"),
        Tag.of("success", "false"))
      ))
  }}

}
