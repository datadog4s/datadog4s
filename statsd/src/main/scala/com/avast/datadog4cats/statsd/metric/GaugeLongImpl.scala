package com.avast.datadog4cats.statsd.metric

import cats.effect.Sync
import com.avast.datadog4cats.api.Tag
import com.avast.datadog4cats.api.metric.Gauge
import com.timgroup.statsd.StatsDClient
import scala.collection.immutable.Seq

class GaugeLongImpl[F[_]: Sync](
  statsDClient: StatsDClient,
  aspect: String,
  sampleRate: Double,
  defaultTags: Seq[Tag]
) extends Gauge[F, Long] {
  private[this] val F = Sync[F]
  override def set(value: Long, tags: Tag*): F[Unit] = F.delay {
    statsDClient.recordGaugeValue(aspect, sampleRate, value, (tags ++ defaultTags): _*)
  }
}
