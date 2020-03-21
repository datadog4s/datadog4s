package com.avast.cloud.datadog4s.common

import java.time.Duration

import cats.effect.concurrent.Ref
import cats.effect.{ ContextShift, IO, Timer }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class RepeatedTest extends AnyFlatSpec with Matchers {
  private val ec: ExecutionContext            = scala.concurrent.ExecutionContext.Implicits.global
  implicit val contextShift: ContextShift[IO] = cats.effect.IO.contextShift(ec)
  implicit val timer: Timer[IO]               = IO.timer(ec)

  val noopErrHandler: Throwable => IO[Unit] = (_: Throwable) => IO.unit

  "repeated test" should "be called repeatedly" in {
    val test = Ref.of[IO, Int](0).flatMap { ref =>
      val forever =
        Repeated.run[IO](Duration.ofMillis(0), Duration.ofMillis(5), Duration.ofMillis(50), noopErrHandler) {
          ref.update(_ + 1)
        }
      forever.use(_ => IO.never).timeout(100 milli).flatMap(_ => ref.get)
    }
    val value = test.unsafeRunSync()
    value must be > 5
  }

  it should "handle errors using provided handler" in {
    val test = Ref.of[IO, ErrorState](ErrorState.empty).flatMap { ref =>
      val forever =
        Repeated.run(Duration.ofMillis(0), Duration.ofMillis(5), Duration.ofMillis(50), _ => ref.update(_.incFail)) {
          IO.raiseError(new Throwable)
        }

      forever.use(_ => IO.never).timeout(100 milli).flatMap(_ => ref.get)
    }
    val value = test.unsafeRunSync()
    value.succ must be(0)
    value.failure must be > 0
  }

  it should "timeout tasks that are taking too long" in {
    val test = Ref.of[IO, ErrorState](ErrorState.empty).flatMap { ref =>
      val forever =
        Repeated.run(Duration.ofMillis(0), Duration.ofMillis(5), Duration.ofMillis(10), _ => ref.update(_.incFail)) {
          IO.never
        }

      forever.use(_ => IO.never).timeout(100 milli).flatMap(_ => ref.get)
    }
    val value = test.unsafeRunSync()
    value.succ must be(0)
    value.failure must be > 0
    value.failure must be <= 10
  }

  case class ErrorState(succ: Int, failure: Int) {
    def incFail: ErrorState = this.copy(failure = failure + 1)
    def incSucc: ErrorState = this.copy(succ = succ + 1)
  }
  object ErrorState {
    def empty: ErrorState = ErrorState(0, 0)
  }

}
