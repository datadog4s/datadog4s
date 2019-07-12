package com.avast.cloud.metrics.datadog.api

import com.avast.cloud.metrics.datadog.api.metric.Histogram

trait HistogramFactory[F[_]] {
  def long(aspect: String, sampleRate: Double = 1.0): Histogram[F, Long]
  def double(aspect: String, sampleRate: Double = 1.0): Histogram[F, Double]
}
