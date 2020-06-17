package com.avast.datadog4s.statsd

import java.util.concurrent.TimeUnit

import cats.effect.{ Clock, IO }
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.statsd.metric.TimerImpl
import com.timgroup.statsd.{ StatsDClient => JStatsDClient }
import org.mockito.scalatest.MockitoSugar
import org.scalatest.{ Assertions, BeforeAndAfter }
import org.scalatest.flatspec.AnyFlatSpec

class TimerImplTest extends AnyFlatSpec with MockitoSugar with BeforeAndAfter with Assertions {

  trait Fixtures {
    val aspect: String = "metric"
    val sampleRate     = 1.0

    val statsD: JStatsDClient = mock[JStatsDClient]
    val clock: Clock[IO]      = mock[Clock[IO]]

    val timer = new TimerImpl[IO](clock, statsD, aspect, sampleRate, Vector.empty)

    when(clock.monotonic(TimeUnit.NANOSECONDS)).thenReturn(IO.pure(10 * 1000 * 1000), IO.pure(30 * 1000 * 1000))
  }

  "time F[A]" should "report success with label success:true" in new Fixtures {
    private val res = timer.time(IO.delay("hello world")).unsafeRunSync()

    verify(statsD, times(1)).recordExecutionTime(aspect, 20, sampleRate, Tag.of("success", "true"))
    assertResult(res)("hello world")
  }

  it should "report failure with label failure:true and exception name" in new Fixtures {
    private val res = timer.time(IO.raiseError(new NoSuchElementException("fail")))

    assertThrows[NoSuchElementException](res.unsafeRunSync())
    verify(statsD, times(1))
      .recordExecutionTime(
        aspect,
        20,
        sampleRate,
        Tag.of("exception", "java.util.NoSuchElementException"),
        Tag.of("success", "false")
      )
  }

}
