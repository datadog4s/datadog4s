package com.avast.cloud.metrics.datadog.api

import scala.language.higherKinds

trait MetricFactory[F[_]] {
  def histogram(aspect: String, sampleRate: Double = 1.0): Histogram[F]
  def timer(aspect: String, sampleRate: Double = 1.0): Timer[F]
  def count(aspect: String, sampleRate: Double = 1.0): Count[F]
  def gauge(aspect: String, sampleRate: Double = 1.0): Gauge[F]
  def uniqueSet(aspect: String): UniqueSet[F]
}
