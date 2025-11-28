package io.github.datadog4s.api

import io.github.datadog4s.api.metric.Histogram

trait HistogramFactory[F[_]] {
  def long(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Long]
  def double(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Double]
}
