package io.github.datadog4s.statsd

import cats.Applicative
import cats.effect.unsafe.IORuntime
import cats.effect.{Clock, IO, Ref}

import scala.concurrent.duration.*

class MockClock(implicit r: IORuntime) extends Clock[IO] {
  val callCount: Ref[IO, Int] = Ref.of[IO, Int](0).unsafeRunSync()

  override def realTime: IO[FiniteDuration] = ???

  override def monotonic: IO[FiniteDuration] =
    callCount.get.flatMap { count =>
      if (count == 0)
        callCount.update(_ + 1) >> IO.pure((10L * 1000 * 1000).nano)
      else
        IO.pure((30L * 1000 * 1000).nano)
    }

  override def applicative: Applicative[IO] = Applicative[IO]
}
