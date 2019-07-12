package com.avast.cloud.metrics.datadog.api

import com.avast.cloud.metrics.datadog.api.metric.Histogram

trait HistogramFactory[F[_]] {
  def long(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Long]
  def double(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Double]
}
