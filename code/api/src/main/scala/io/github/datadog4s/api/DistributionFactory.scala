package io.github.datadog4s.api

import io.github.datadog4s.api.metric.Distribution

trait DistributionFactory[F[_]] {
  def long(aspect: String, sampleRate: Option[Double] = None): Distribution[F, Long]
  def double(aspect: String, sampleRate: Option[Double] = None): Distribution[F, Double]

}
