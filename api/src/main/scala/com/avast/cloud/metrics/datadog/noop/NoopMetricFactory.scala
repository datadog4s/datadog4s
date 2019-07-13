package com.avast.cloud.metrics.datadog.noop

import cats.Applicative
import com.avast.cloud.metrics.datadog.api._
import com.avast.cloud.metrics.datadog.api.metric._
import com.avast.cloud.metrics.datadog.noop.metric._

class NoopMetricFactory[F[_]: Applicative] extends MetricFactory[F] {
  override def timer(
    prefix: String,
    sampleRate: Option[Double] = None,
    exceptionTagging: Option[Boolean] = None
  ): Timer[F] = new NoopTimer[F]

  override def count(prefix: String, sampleRate: Option[Double] = None): Count[F] = new NoopCount[F]

  override def histogram: HistogramFactory[F] = new HistogramFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Long] = new NoopHistogramLong[F]

    override def double(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Double] =
      new NoopHistogramDouble[F]
  }

  override def gauge: GaugeFactory[F] = new GaugeFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Long] = new NoopGaugeLong[F]

    override def double(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Double] = new NoopGaugeDouble[F]
  }

  override def uniqueSet(aspect: String): UniqueSet[F] = new NoopUniqueSet[F]

  override def withTags(tags: Tag*): MetricFactory[F] = this
}
