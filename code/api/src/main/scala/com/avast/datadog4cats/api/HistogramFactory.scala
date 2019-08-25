package com.avast.datadog4cats.api

import com.avast.datadog4cats.api.metric.Histogram

trait HistogramFactory[F[_]] {
  def long(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Long]
  def double(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Double]
}
