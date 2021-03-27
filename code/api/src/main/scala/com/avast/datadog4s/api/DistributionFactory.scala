package com.avast.datadog4s.api

import com.avast.datadog4s.api.metric.Distribution

trait DistributionFactory[F[_]] {
  def long(aspect: String, sampleRate: Option[Double] = None): Distribution[F, Long]
  def double(aspect: String, sampleRate: Option[Double] = None): Distribution[F, Double]
}
