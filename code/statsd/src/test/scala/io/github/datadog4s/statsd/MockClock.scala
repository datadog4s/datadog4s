package io.github.datadog4s.statsd

import java.util.concurrent.TimeUnit

import cats.effect.concurrent.Ref
import cats.effect.{Clock, IO}
import cats.implicits.catsSyntaxFlatMapOps

class MockClock extends Clock[IO] {
  val callCount = Ref.of[IO, Int](0).unsafeRunSync()

  override def realTime(unit: TimeUnit): IO[Long] = ???

  override def monotonic(unit: TimeUnit): IO[Long] =
    callCount.get.flatMap { count =>
      if (count == 0)
        callCount.update(_ + 1) >> IO.pure(10L * 1000 * 1000)
      else
        IO.pure(30L * 1000 * 1000)
    }

}
