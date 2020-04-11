package com.avast.cloud.datadog4s.inmemory

import java.time.Duration

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.avast.datadog4s.api.event.Event
import com.avast.datadog4s.api.metric._
import com.avast.datadog4s.api.{ GaugeFactory, HistogramFactory, MetricFactory, Tag }

class MockMetricsFactory[F[_]: Sync](val state: Ref[F, Map[String, Vector[Record[Any]]]]) extends MetricFactory[F] {

  private def updateState[A](aspect: String, value: A, tags: Tag*): F[Unit] =
    state.update { oldState =>
      val updatedField = oldState.getOrElse(aspect, Vector.empty) :+ Record[Any](value, tags)
      oldState.updated(aspect, updatedField)
    }.void

  override def histogram: HistogramFactory[F] = new HistogramFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double]): Histogram[F, Long] = new Histogram[F, Long] {
      override def record(value: Long, tags: Tag*): F[Unit] = updateState(aspect, value, tags: _*)
    }

    override def double(aspect: String, sampleRate: Option[Double]): Histogram[F, Double] = new Histogram[F, Double] {
      override def record(value: Double, tags: Tag*): F[Unit] = updateState(aspect, value, tags: _*)
    }
  }

  override def gauge: GaugeFactory[F] = new GaugeFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double]): Gauge[F, Long] = new Gauge[F, Long] {
      override def set(value: Long, tags: Tag*): F[Unit] = updateState(aspect, value, tags: _*)

    }

    override def double(aspect: String, sampleRate: Option[Double]): Gauge[F, Double] = new Gauge[F, Double] {
      override def set(value: Double, tags: Tag*): F[Unit] = updateState(aspect, value, tags: _*)
    }
  }

  override def timer(aspect: String, sampleRate: Option[Double]): Timer[F] = new Timer[F] {
    override def time[A](f: F[A], tags: Tag*): F[A] = f.flatMap(a => updateState(aspect, a, tags: _*).as(a))

    override def record(duration: Duration, tags: Tag*): F[Unit] = updateState[Duration](aspect, duration, tags: _*)
  }

  override def count(aspect: String, sampleRate: Option[Double]): Count[F] = new Count[F] {
    override def modify(delta: Int, tags: Tag*): F[Unit] = updateState(aspect, delta, tags: _*)
  }

  override def eventer: Eventer[F] = new Eventer[F] {
    override def send(event: Event, tags: Tag*): F[Unit] = updateState("", event, tags: _*)
  }

  override def uniqueSet(aspect: String): UniqueSet[F] = new UniqueSet[F] {
    override def record(value: String, tags: Tag*): F[Unit] = updateState(aspect, value, tags: _*)
  }

  override def withTags(tags: Tag*): MetricFactory[F] = this

  override def withScope(name: String): MetricFactory[F] = this
}

object MockMetricsFactory {

  def make[F[_]: Sync]: F[MockMetricsFactory[F]] =
    Ref.of(Map.empty[String, Vector[Record[Any]]]).map(state => new MockMetricsFactory[F](state))
}
