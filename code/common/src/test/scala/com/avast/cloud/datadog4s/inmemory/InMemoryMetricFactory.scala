package com.avast.cloud.datadog4s.inmemory

import java.time.Duration
import java.util.concurrent.{ ConcurrentHashMap, ConcurrentMap }

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.avast.datadog4s.api.metric._
import com.avast.datadog4s.api.{ GaugeFactory, HistogramFactory, MetricFactory, Tag }

class InMemoryMetricFactory[F[_]: Sync](val state: ConcurrentMap[String, Vector[Record[Any]]])
    extends MetricFactory[F] {

  private def updateState[A](aspect: String, value: A, tags: Tag*): F[Unit] =
    Sync[F].delay {
      state.computeIfPresent(aspect, (_, v) => v :+ Record[Any](value, tags))
      state.computeIfAbsent(aspect, _ => Vector(Record[Any](value, tags)))
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

  override def uniqueSet(aspect: String): UniqueSet[F] = new UniqueSet[F] {
    override def record(value: String, tags: Tag*): F[Unit] = updateState(aspect, value, tags: _*)
  }

  override def withTags(tags: Tag*): MetricFactory[F] = this

  override def withScope(name: String): MetricFactory[F] = this
}

object InMemoryMetricFactory {
  def make[F[_]: Sync] = new InMemoryMetricFactory[F](new ConcurrentHashMap[String, Vector[Record[Any]]]())
}
