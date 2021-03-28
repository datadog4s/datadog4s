package com.avast.datadog4s.api

import com.avast.datadog4s.api.metric.{ Count, Timer, UniqueSet }

trait MetricFactory[F[_]] {
  def histogram: HistogramFactory[F]
  def distribution: DistributionFactory[F]
  def gauge: GaugeFactory[F]
  @deprecated()
  def timer(aspect: String, sampleRate: Option[Double] = None): Timer[F]
  def timer: TimerFactory[F]
  def count(aspect: String, sampleRate: Option[Double] = None): Count[F]
  def uniqueSet(aspect: String): UniqueSet[F]

  def withTags(tags: Tag*): MetricFactory[F]
  def withScope(name: String): MetricFactory[F]
}
