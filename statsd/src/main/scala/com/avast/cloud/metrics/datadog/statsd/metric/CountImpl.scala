package com.avast.cloud.metrics.datadog.statsd.metric

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.Count
import com.timgroup.statsd.StatsDClient
import scala.collection.immutable.Seq

class CountImpl[F[_]: Sync](statsDClient: StatsDClient, prefix: String, sampleRate: Double, defaultTags: Seq[Tag])
    extends Count[F] {
  private[this] val F = Sync[F]

  override def modify(delta: Int, tags: Tag*): F[Unit] =
    F.delay(statsDClient.count(prefix, delta, sampleRate, (tags ++ defaultTags): _*))

}
