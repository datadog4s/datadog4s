package io.github.datadog4s.inmemory

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import io.github.datadog4s.api.metric.*
import io.github.datadog4s.api.*

import java.util.concurrent.TimeUnit

class MockMetricsFactory[F[_]: Sync](val state: Ref[F, Map[String, Vector[Record[Any]]]]) extends MetricFactory[F] {

  private def updateState[A](aspect: String, value: A, tags: Tag*): F[Unit] =
    state.update { oldState =>
      val updatedField = oldState.getOrElse(aspect, Vector.empty) :+ Record[Any](value, tags)
      oldState.updated(aspect, updatedField)
    }.void

  override def histogram: HistogramFactory[F] =
    new HistogramFactory[F] {
      override def long(aspect: String, sampleRate: Option[Double]): Histogram[F, Long] =
        new Histogram[F, Long] {
          override def record(value: Long, tags: Tag*): F[Unit] = updateState(aspect, value, tags*)
        }

      override def double(aspect: String, sampleRate: Option[Double]): Histogram[F, Double] =
        new Histogram[F, Double] {
          override def record(value: Double, tags: Tag*): F[Unit] = updateState(aspect, value, tags*)
        }
    }

  override def gauge: GaugeFactory[F] =
    new GaugeFactory[F] {
      override def long(aspect: String, sampleRate: Option[Double]): Gauge[F, Long] =
        new Gauge[F, Long] {
          override def set(value: Long, tags: Tag*): F[Unit] = updateState(aspect, value, tags*)

        }

      override def double(aspect: String, sampleRate: Option[Double]): Gauge[F, Double] =
        new Gauge[F, Double] {
          override def set(value: Double, tags: Tag*): F[Unit] = updateState(aspect, value, tags*)
        }
    }

  override def timer(aspect: String, sampleRate: Option[Double]): Timer[F] =
    new Timer[F] {
      override def time[A](f: F[A], tags: Tag*): F[A] = f.flatMap(a => updateState(aspect, a, tags*).as(a))

      override def record[T: ElapsedTime](duration: T, tags: Tag*): F[Unit] =
        updateState[Long](aspect, ElapsedTime[T].amount(duration, TimeUnit.MILLISECONDS), tags*)
    }

  override def count(aspect: String, sampleRate: Option[Double]): Count[F] =
    new Count[F] {
      override def modify(delta: Int, tags: Tag*): F[Unit] = updateState(aspect, delta, tags*)
    }

  override def uniqueSet(aspect: String): UniqueSet[F] =
    new UniqueSet[F] {
      override def record(value: String, tags: Tag*): F[Unit] = updateState(aspect, value, tags*)
    }

  override def distribution: DistributionFactory[F] =
    new DistributionFactory[F] {
      override def long(aspect: String, sampleRate: Option[Double]): Distribution[F, Long] =
        new Distribution[F, Long] {
          override def record(value: Long, tags: Tag*): F[Unit] = updateState(aspect, value, tags*)
        }

      override def double(aspect: String, sampleRate: Option[Double]): Distribution[F, Double] =
        new Distribution[F, Double] {
          override def record(value: Double, tags: Tag*): F[Unit] = updateState(aspect, value, tags*)
        }
    }

  override def timer: TimerFactory[F] = new TimerFactory[F] {
    override def histogram(aspect: String, sampleRate: Option[Double], timeUnit: TimeUnit): Timer[F] = new Timer[F] {
      override def time[A](f: F[A], tags: Tag*): F[A] = f.flatMap(a => updateState(aspect, a, tags*).as(a))

      override def record[T: ElapsedTime](duration: T, tags: Tag*): F[Unit] =
        updateState[Long](aspect, ElapsedTime[T].amount(duration, timeUnit), tags*)
    }

    override def distribution(aspect: String, sampleRate: Option[Double], timeUnit: TimeUnit): Timer[F] = new Timer[F] {
      override def time[A](f: F[A], tags: Tag*): F[A] = f.flatMap(a => updateState(aspect, a, tags*).as(a))

      override def record[T: ElapsedTime](duration: T, tags: Tag*): F[Unit] =
        updateState[Long](aspect, ElapsedTime[T].amount(duration, timeUnit), tags*)
    }

  }

  override def withTags(tags: Tag*): MetricFactory[F] = this

  override def withScope(name: String): MetricFactory[F] = this

}

object MockMetricsFactory {

  def make[F[_]: Sync]: F[MockMetricsFactory[F]] =
    Ref.of(Map.empty[String, Vector[Record[Any]]]).map(state => new MockMetricsFactory[F](state))
}
