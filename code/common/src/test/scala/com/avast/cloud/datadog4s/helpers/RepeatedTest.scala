package com.avast.cloud.datadog4s.helpers

import java.time.Duration

import cats.effect.concurrent.{ Deferred, Ref }
import cats.effect.{ ContextShift, IO, Timer }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class RepeatedTest extends AnyFlatSpec with Matchers {
  private val ec: ExecutionContext                  = scala.concurrent.ExecutionContext.Implicits.global
  implicit val contextShift: ContextShift[IO]       = cats.effect.IO.contextShift(ec)
  implicit val timer: Timer[IO]                     = IO.timer(ec)
  private val logger                                = LoggerFactory.getLogger(classOf[RepeatedTest])
  private val noopErrHandler: Throwable => IO[Unit] = (_: Throwable) => IO.unit

  "repeated test" should "be called repeatedly" in {
    val waitFor = 10

    def buildProcess(counter: Ref[IO, Int], killSignal: Deferred[IO, Unit]): IO[Int] = {
      def decreaseCounter: IO[Unit] =
        counter.modify { currentCount =>
          currentCount - 1 match {
            case newCounter if newCounter <= 0 => (newCounter, killSignal.complete(()))
            case newCounter                    => (newCounter, IO.pure(()))
          }
        }.flatMap(identity)

      val process = Repeated.run[IO](Duration.ofMillis(5), Duration.ofMillis(50), noopErrHandler) {
        IO.delay(logger.info("increasing ref")) *> decreaseCounter *> IO.delay(logger.info("ref updated"))
      }
      process.use(_ => killSignal.get) *> counter.get
    }

    val test = for {
      killSignal <- Deferred[IO, Unit]
      counter    <- Ref.of[IO, Int](waitFor)
      output     <- buildProcess(counter, killSignal)
    } yield {
      output
    }

    val value = (IO.delay(logger.info("starting test")) *> test)
      .timeout(1 minute) //failsafe in case it all runs forever
      .attempt
      .unsafeRunSync()

    logger.info(s"test finished with $value")
    value.fold(throw _, identity) must equal(0)
  }

  it should "handle errors using provided handler" in {
    val test = Ref.of[IO, ErrorState](ErrorState.empty).flatMap { ref =>
      val forever =
        Repeated.run(Duration.ofMillis(5), Duration.ofMillis(50), _ => ref.update(_.incFail)) {
          IO.raiseError(new Throwable)
        }

      forever.use(_ => IO.never).timeout(100 milli).attempt.flatMap(_ => ref.get)
    }
    val value = test.unsafeRunSync()
    logger.info(s"test finished with $value")
    value.succ must be(0)
    value.failure must be > 0
  }

  it should "timeout tasks that are taking too long" in {
    val test = Ref.of[IO, ErrorState](ErrorState.empty).flatMap { ref =>
      val forever =
        Repeated.run(Duration.ofMillis(5), Duration.ofMillis(10), _ => ref.update(_.incFail)) {
          IO.never
        }

      forever.use(_ => IO.never).timeout(100 milli).attempt.flatMap(_ => ref.get)
    }
    val value = test.unsafeRunSync()
    logger.info(s"test finished with $value")
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
