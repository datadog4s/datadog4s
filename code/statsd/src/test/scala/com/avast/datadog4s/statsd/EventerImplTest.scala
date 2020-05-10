package com.avast.datadog4s.statsd

import cats.effect.IO
import com.avast.datadog4s.api.event.Event.Info
import com.avast.datadog4s.statsd.event.EventerImpl
import com.timgroup.statsd.{ StatsDClient, Event => SEvent }
import org.mockito.ArgumentMatchers
import org.mockito.scalatest.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.{ Assertions, BeforeAndAfter }
class EventerImplTest extends AnyFlatSpec with MockitoSugar with BeforeAndAfter with Assertions {

  trait Fixtures {
    val statsD: StatsDClient = mock[StatsDClient]

    val eventer = new EventerImpl[IO](statsD, Vector.empty)
  }

  "eventer F[A]" should "report passed event" in new Fixtures {

    eventer.send(Info("hello", "world")).unsafeRunSync()

    val expectedEvent: SEvent = SEvent
      .builder()
      .withTitle("hello")
      .withText("world")
      .withPriority(SEvent.Priority.NORMAL)
      .withAlertType(SEvent.AlertType.INFO)
      .build()
    verify(statsD, times(1)).recordEvent(ArgumentMatchers.refEq(expectedEvent))
  }

}
