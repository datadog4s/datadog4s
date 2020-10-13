package com.avast.cloud.datadog4s.helpers

import java.time.Duration

import cats.effect.concurrent.{ Deferred, Ref }
import cats.effect.{ ContextShift, IO, Timer }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.slf4j.LoggerFactory
import cats.syntax.flatMap._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import scala.language.postfixOps

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
            case newCounter                    => (newCounter, IO.unit)
          }
        }.flatten

      val process = Repeated.run[IO](Duration.ofMillis(5), Duration.ofMillis(50), noopErrHandler) {
        IO.delay(logger.info("increasing ref")) *> decreaseCounter *> IO.delay(logger.info("ref updated"))
      }
      process.use(_ => killSignal.get) *> counter.get
    }

    val test = for {
      killSignal <- Deferred[IO, Unit]
      counter    <- Ref.of[IO, Int](waitFor)
      output     <- buildProcess(counter, killSignal)
    } yield output

    val value = (IO.delay(logger.info("starting test")) *> test)
      .timeout(1 minute) //failsafe in case it all runs forever
      .attempt
      .unsafeRunSync()

    logger.info(s"test finished with $value")
    value.fold(throw _, identity) must be <= 0
  }

  it should "handle errors using provided handler" in {
    val test  = for {
      ref        <- Ref.of[IO, ErrorState](ErrorState.empty)
      killSignal <- Deferred[IO, Unit]
    } yield {
      val process = Repeated.run(
        Duration.ofMillis(5),
        Duration.ofMillis(50),
        _ => ref.update(_.incFail) *> killSignal.complete(())
      ) {
        IO.raiseError(new Throwable)
      }
      process.use(_ => killSignal.get) *> ref.get
    }
    val value = test.flatten.timeout(100 milli).attempt.unsafeRunSync().fold(throw _, identity)
    logger.info(s"test finished with $value")
    value.succ must be(0)
    value.failure must be > 0
  }

  it should "timeout tasks that are taking too long" in {
    val test = for {
      ref        <- Ref.of[IO, ErrorState](ErrorState.empty)
      killSignal <- Deferred[IO, Unit]
    } yield {
      val process = Repeated.run(
        Duration.ofMillis(5),
        Duration.ofMillis(10),
        _ => ref.update(_.incFail) *> killSignal.complete(())
      ) {
        IO.never
      }
      process.use(_ => killSignal.get) *> ref.get
    }

    val result = test.flatten.timeout(100 milli).attempt.unsafeRunSync().fold(throw _, identity)

    logger.info(s"test finished with $result")
    result.succ must be(0)
    result.failure must be > 0
    result.failure must be <= 10
  }

  case class ErrorState(succ: Int, failure: Int) {
    def incFail: ErrorState = this.copy(failure = failure + 1)
    def incSucc: ErrorState = this.copy(succ = succ + 1)
  }
  object ErrorState                              {
    def empty: ErrorState = ErrorState(0, 0)
  }

}
