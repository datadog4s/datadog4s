package io.github.datadog4s.helpers

import java.time.Duration
import cats.effect.{Concurrent, Outcome, Resource, Temporal}
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.apply.*
import cats.syntax.applicative.*

import scala.concurrent.duration.*

object Repeated {

  /** Creates a never ending process that will run `task` periodically. The resource provided has the same semantics as
    * `Concurrent.background()``
    * @param delay
    *   Delay between each run of `task`
    * @param iterationTimeout
    *   timeout applied to `task`, ideally it should be lower than `delay`
    * @param errorHandler
    *   handler called when `task fails or during timeout
    * @param task
    *   effect that will be run periodically
    */
  def run[F[_]: Temporal](
      delay: Duration,
      iterationTimeout: Duration,
      errorHandler: Throwable => F[Unit]
  )(task: F[Unit]): Resource[F, F[Outcome[F, Throwable, Unit]]] = {
    val safeTask = Temporal[F].timeout(task, toScala(iterationTimeout)).attempt.flatMap {
      case Right(a) => a.pure[F]
      case Left(e)  => errorHandler(e)
    }

    val snooze  = Temporal[F].sleep(toScala(delay))
    val process = (safeTask *> snooze).foreverM[Unit]

    Concurrent[F].background(process)
  }

  private def toScala(duration: Duration): FiniteDuration =
    duration.toMillis.millis
}
