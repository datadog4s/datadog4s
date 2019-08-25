package com.avast.datadog4cats.api

import com.avast.datadog4cats.api.metric.Gauge

trait GaugeFactory[F[_]] {
  def long(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Long]
  def double(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Double]
}
