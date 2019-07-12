package com.avast.cloud.metrics.datadog.impl

import java.util.concurrent.TimeUnit

import cats.effect.{ Clock, IO }
import com.avast.cloud.metrics.datadog.api.Tag
import com.timgroup.statsd.StatsDClient
import org.mockito.scalatest.MockitoSugar
import org.scalatest.{ Assertions, BeforeAndAfter, FlatSpec }

class TimerImplTest extends FlatSpec with MockitoSugar with BeforeAndAfter with Assertions {

  trait Fixtures {
    val aspect: String = "metric"
    val sampleRate     = 1

    val statsD: StatsDClient = mock[StatsDClient]
    val clock: Clock[IO]     = mock[Clock[IO]]

    val timer = new TimerImpl[IO](clock, statsD, aspect, sampleRate)

    when(clock.monotonic(TimeUnit.MILLISECONDS)).thenReturn(IO.pure(10), IO.pure(30))
  }

  "time F[A]" should "report success with label success:true" in new Fixtures {
    private val res = timer.time(IO.delay("hello world")).unsafeRunSync()

    verify(statsD, times(1)).recordExecutionTime(aspect, 20, sampleRate, Tag.of("success", "true"))
    assertResult(res) { "hello world" }
  }

  it should "report failure with label failure:true and exception name" in new Fixtures {
    private val res = timer.time(IO.raiseError(new NoSuchElementException("fail")))

    assertThrows[NoSuchElementException](res.unsafeRunSync())
    verify(statsD, times(1))
      .recordExecutionTime(aspect,
                           20,
                           sampleRate,
                           Tag.of("success", "false"),
                           Tag.of("exception", "java.util.NoSuchElementException"))
  }

}
