package com.avast.cloud.metrics.datadog.noop

import cats.Monad
import com.avast.cloud.metrics.datadog.api._

import scala.language.higherKinds

class NoopMetricFactory[F[_]: Monad] extends MetricFactory[F] {
  override def timer(prefix: String, sampleRate: Double = 1.0): Timer[F] = new NoopTimer[F]

  override def count(prefix: String, sampleRate: Double = 1.0): Count[F] = new NoopCount[F]

  override def histogram(aspect: String, sampleRate: Double = 1.0): Histogram[F] = new NoopHistogram[F]

  override def gauge(aspect: String, sampleRate: Double = 1.0): Gauge[F] = new NoopGauge[F]

  override def uniqueSet(aspect: String): UniqueSet[F] = new NoopUniqueSet[F]
}
