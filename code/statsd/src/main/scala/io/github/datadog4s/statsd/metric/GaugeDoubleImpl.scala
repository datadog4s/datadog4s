package io.github.datadog4s.statsd.metric

import cats.effect.Sync
import com.timgroup.statsd.StatsDClient
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.Gauge
import scala.collection.immutable.Seq

class GaugeDoubleImpl[F[_]: Sync](
    statsDClient: StatsDClient,
    aspect: String,
    sampleRate: Double,
    defaultTags: Seq[Tag]
) extends Gauge[F, Double] {
  private val F = Sync[F]

  override def set(value: Double, tags: Tag*): F[Unit] =
    F.delay {
      statsDClient.recordGaugeValue(aspect, value, sampleRate, (tags ++ defaultTags)*)
    }
}
