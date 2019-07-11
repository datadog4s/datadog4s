package com.avast.cloud.metrics.datadog.impl

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.{Gauge, Tag}
import com.timgroup.statsd.StatsDClient

import scala.language.higherKinds

class GaugeImpl[F[_]: Sync](statsDClient: StatsDClient, aspect: String, sampleRate: Double) extends Gauge[F] {
  private[this] val F = Sync[F] 
  override def set(value: Long, tags: Tag*): F[Unit] = F.delay {
    statsDClient.recordGaugeValue(aspect, sampleRate, value, tags: _*)
  }

  override def set(value: Double, tags: Tag*): F[Unit] = F.delay {
    statsDClient.recordGaugeValue(aspect, sampleRate, value, tags: _*)
  }
}
