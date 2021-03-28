package com.avast.datadog4s.api

import com.avast.datadog4s.api.metric.Timer

trait TimerFactory[F[_]] {
  def histogram(aspect: String, sampleRate: Option[Double] = None): Timer[F]
  def distribution(aspect: String, sampleRate: Option[Double] = None): Timer[F]
}
