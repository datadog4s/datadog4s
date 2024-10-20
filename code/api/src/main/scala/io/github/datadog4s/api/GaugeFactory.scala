package io.github.datadog4s.api

import io.github.datadog4s.api.metric.Gauge

trait GaugeFactory[F[_]] {
  def long(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Long]
  def double(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Double]
}
