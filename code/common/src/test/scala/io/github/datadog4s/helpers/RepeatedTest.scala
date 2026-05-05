package io.github.datadog4s.helpers

import cats.effect.{Deferred, IO, Ref}
import org.slf4j.LoggerFactory

import java.time.Duration
import scala.concurrent.duration.*
import scala.language.postfixOps

class RepeatedTest extends munit.FunSuite {
  import cats.effect.unsafe.implicits.global
  private val logger                                = LoggerFactory.getLogger(classOf[RepeatedTest])
  private val noopErrHandler: Throwable => IO[Unit] = (_: Throwable) => IO.unit

  test("repeated test should be called repeatedly") {
    val waitFor = 10

    def buildProcess(counter: Ref[IO, Int], killSignal: Deferred[IO, Unit]): IO[Int] = {
      def decreaseCounter: IO[Unit] =
        counter.modify { currentCount =>
          currentCount - 1 match {
            case newCounter if newCounter <= 0 => (newCounter, killSignal.complete(()).void)
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
      .timeout(1 minute) // failsafe in case it all runs forever
      .attempt
      .unsafeRunSync()

    logger.info(s"test finished with $value")
    assert(value.fold(throw _, identity) <= 0)
  }

  test("repeated test should handle errors using provided handler") {
    val test = for {
      ref        <- Ref.of[IO, ErrorState](ErrorState.empty)
      killSignal <- Deferred[IO, Unit]
    } yield {
      val process = Repeated.run(
        Duration.ofMillis(5),
        Duration.ofMillis(50),
        _ => ref.update(_.incFail) *> killSignal.complete(()).void
      ) {
        IO.raiseError(new Throwable)
      }
      process.use(_ => killSignal.get) *> ref.get
    }
    val value = test.flatten.timeout(500 milli).attempt.unsafeRunSync().fold(throw _, identity)
    logger.info(s"test finished with $value")
    assert(value.succ == 0)
    assert(value.failure > 0)
  }

  test("repeated test should timeout tasks that are taking too long") {
    val test = for {
      ref        <- Ref.of[IO, ErrorState](ErrorState.empty)
      killSignal <- Deferred[IO, Unit]
    } yield {
      val process = Repeated.run(
        Duration.ofMillis(5),
        Duration.ofMillis(10),
        _ => ref.update(_.incFail) *> killSignal.complete(()).void
      ) {
        IO.never
      }
      process.use(_ => killSignal.get) *> ref.get
    }

    val result = test.flatten.timeout(3000 milli).attempt.unsafeRunSync().fold(throw _, identity)

    logger.info(s"test finished with $result")
    assert(result.succ == 0)
    assert(result.failure > 0)
    assert(result.failure <= 10)
  }

  case class ErrorState(succ: Int, failure: Int) {
    def incFail: ErrorState = this.copy(failure = failure + 1)
    def incSucc: ErrorState = this.copy(succ = succ + 1)
  }
  object ErrorState {
    def empty: ErrorState = ErrorState(0, 0)
  }

}
