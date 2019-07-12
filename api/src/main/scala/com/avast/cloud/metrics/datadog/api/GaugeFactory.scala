package com.avast.cloud.metrics.datadog.api

import com.avast.cloud.metrics.datadog.api.metric.Gauge

trait GaugeFactory[F[_]] {
  def long(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Long]
  def double(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Double]
}
