package com.avast.datadog4cats.api

import com.avast.datadog4cats.api.metric.{ Count, Timer, UniqueSet }

trait MetricFactory[F[_]] {
  def histogram: HistogramFactory[F]
  def gauge: GaugeFactory[F]
  def timer(aspect: String, sampleRate: Option[Double] = None): Timer[F]
  def count(aspect: String, sampleRate: Option[Double] = None): Count[F]
  def uniqueSet(aspect: String): UniqueSet[F]

  def withTags(tags: Tag*): MetricFactory[F]
  def withScope(name: String): MetricFactory[F]
}
