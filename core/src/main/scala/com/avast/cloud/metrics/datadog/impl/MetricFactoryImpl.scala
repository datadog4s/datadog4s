package com.avast.cloud.metrics.datadog.impl

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.MetricFactory
import com.timgroup.statsd.StatsDClient

import scala.language.higherKinds

class MetricFactoryImpl[F[_]: Sync](statsDClient: StatsDClient) extends MetricFactory[F] {
  def timer(prefix: String, sampleRate: Double = 1.0) =
    new TimerImpl[F](statsDClient, prefix, sampleRate)
  def count(prefix: String, sampleRate: Double = 1.0) =
    new CountImpl[F](statsDClient, prefix, sampleRate)
}
