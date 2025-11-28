package io.github.datadog4s.noop

import cats.Applicative
import io.github.datadog4s.api.*
import io.github.datadog4s.api.metric.*
import io.github.datadog4s.noop.metric.*

import java.util.concurrent.TimeUnit

class NoopMetricFactory[F[_]: Applicative] extends MetricFactory[F] {
  override def timer(prefix: String, sampleRate: Option[Double] = None): Timer[F] = new NoopTimer[F]

  override def count(prefix: String, sampleRate: Option[Double] = None): Count[F] = new NoopCount[F]

  override def histogram: HistogramFactory[F] =
    new HistogramFactory[F] {
      override def long(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Long] =
        new NoopHistogram[F, Long]

      override def double(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Double] =
        new NoopHistogram[F, Double]
    }

  override def gauge: GaugeFactory[F] =
    new GaugeFactory[F] {
      override def long(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Long] = new NoopGauge[F, Long]

      override def double(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Double] =
        new NoopGauge[F, Double]
    }

  override def distribution: DistributionFactory[F] =
    new DistributionFactory[F] {
      override def long(aspect: String, sampleRate: Option[Double]): Distribution[F, Long] =
        new NoopDistribution[F, Long]

      override def double(aspect: String, sampleRate: Option[Double]): Distribution[F, Double] =
        new NoopDistribution[F, Double]
    }

  override def uniqueSet(aspect: String): UniqueSet[F] = new NoopUniqueSet[F]

  override def withTags(tags: Tag*): MetricFactory[F] = this

  override def withScope(prefix: String): MetricFactory[F] = this

  override def timer: TimerFactory[F] = new TimerFactory[F] {
    override def histogram(aspect: String, sampleRate: Option[Double], timeUnit: TimeUnit): Timer[F] = new NoopTimer[F]

    override def distribution(aspect: String, sampleRate: Option[Double], timeUnit: TimeUnit): Timer[F] =
      new NoopTimer[F]
  }
}
