package com.avast.cloud.datadog4s.helpers

import java.time.Duration
import java.util.concurrent.Executors

import cats.effect.concurrent.Ref
import cats.effect.{ ContextShift, IO, Timer }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class RepeatedTest extends AnyFlatSpec with Matchers {
  private val ec: ExecutionContext                  = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))
  implicit val contextShift: ContextShift[IO]       = cats.effect.IO.contextShift(ec)
  implicit val timer: Timer[IO]                     = IO.timer(ec)
  private val logger                                = LoggerFactory.getLogger(classOf[RepeatedTest])
  private val noopErrHandler: Throwable => IO[Unit] = (_: Throwable) => IO.unit

  "repeated test" should "be called repeatedly" in {
    val waitFor = 10

    val test = Ref.of[IO, Int](0).flatMap { ref =>
      val endlessProcess =
        Repeated.run[IO](Duration.ofMillis(5), Duration.ofMillis(50), noopErrHandler) {
          IO.delay(logger.info("increasing ref")) *> ref.update(_ + 1) *> IO.delay(logger.info("ref updated"))
        }

      def waitUntil: IO[Unit] = ref.get.flatMap {
        case value if value > waitFor => IO.pure(())
        case _                        => timer.sleep(10.millis) *> waitUntil
      }

      val observedProcess = endlessProcess.use(_ => waitUntil)

      IO.delay(logger.info("starting test")) *> observedProcess
        .timeout(1 minute) //failsafe in case it all runs forever
        .attempt
        .flatMap(_ => ref.get)
    }

    val value = test.unsafeRunSync()
    logger.info(s"test finished with $value")
    value must be > waitFor
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
