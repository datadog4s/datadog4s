import java.util.concurrent.TimeUnit

import cats.effect.{Clock, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.timgroup.statsd.StatsDClient

import scala.collection.immutable



class TimerImpl[F[_]: Sync](cli: StatsDClient)(name: String, defaultTags: immutable.Seq[String] = Vector.empty) extends Timer[F] {
  private[this] val F = Sync[F]
  
  

  override def time[A](f: F[A]): F[A] = time(f, Vector.empty: _*)

  override def time[A](f: F[A], tags: String*): F[A] = {
    val clock = Clock.create[F]

    for {
      old <- clock.monotonic(TimeUnit.NANOSECONDS)
      a <- F.recoverWith(f){
        case thr => for {
          now <- clock.monotonic(TimeUnit.NANOSECONDS)
          _ <- F.delay(cli.recordExecutionTime(name, now - old, "success:false", s"exception:${thr.getClass.getName}"))
        } yield {
          F.raiseError(thr)
        }
      }
      now <- clock.monotonic(TimeUnit.NANOSECONDS)
      _ <- F.delay(cli.recordExecutionTime(name, now - old, "success:true"))
    } yield {
      a
    }
  }

  override def time[A](f: () => A): A = ???
} 