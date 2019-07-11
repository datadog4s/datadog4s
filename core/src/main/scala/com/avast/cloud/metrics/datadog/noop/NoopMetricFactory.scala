package com.avast.cloud.metrics.datadog.noop

import cats.Monad
import com.avast.cloud.metrics.datadog.api.{ Count, MetricFactory, Timer }

import scala.language.higherKinds

class NoopMetricFactory[F[_]: Monad] extends MetricFactory[F] {
  override def timer(prefix: String, sampleRate: Double): Timer[F] = new NoopTimer[F]

  override def count(prefix: String, sampleRate: Double): Count[F] = new NoopCount[F]
}
