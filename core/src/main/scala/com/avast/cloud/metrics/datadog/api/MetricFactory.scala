package com.avast.cloud.metrics.datadog.api

import scala.language.higherKinds

trait MetricFactory[F[_]] {
  def timer(prefix: String, sampleRate: Double = 1.0): Timer[F]
  def count(prefix: String, sampleRate: Double = 1.0): Count[F]
}
