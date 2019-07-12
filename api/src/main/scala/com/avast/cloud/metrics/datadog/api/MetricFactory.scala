package com.avast.cloud.metrics.datadog.api

import com.avast.cloud.metrics.datadog.api.metric.{ Count, Timer, UniqueSet }

trait MetricFactory[F[_]] {
  def histogram: HistogramFactory[F]
  def gauge: GaugeFactory[F]
  def timer(aspect: String, sampleRate: Double = 1.0): Timer[F]
  def count(aspect: String, sampleRate: Double = 1.0): Count[F]
  def uniqueSet(aspect: String): UniqueSet[F]
}
