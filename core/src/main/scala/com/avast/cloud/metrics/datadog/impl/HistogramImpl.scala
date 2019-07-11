package com.avast.cloud.metrics.datadog.impl

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.{ Histogram, Tag }
import com.timgroup.statsd.StatsDClient

import scala.language.higherKinds

class HistogramImpl[F[_]: Sync](statsDClient: StatsDClient, aspect: String, sampleRate: Double) extends Histogram[F] {

  private[this] val F = Sync[F]

  override def record(value: Long, tags: Tag*): F[Unit] = F.delay {
    statsDClient.recordHistogramValue(aspect, value, sampleRate, tags: _*)
  }

  override def record(value: Double, tags: Tag*): F[Unit] = F.delay {
    statsDClient.recordHistogramValue(aspect, value, sampleRate, tags: _*)
  }

}
