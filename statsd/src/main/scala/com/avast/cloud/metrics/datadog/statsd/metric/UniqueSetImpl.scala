package com.avast.cloud.metrics.datadog.statsd.metric

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.UniqueSet
import com.timgroup.statsd.StatsDClient

class UniqueSetImpl[F[_]: Sync](statsDClient: StatsDClient, aspect: String, defaultTags: Vector[Tag])
    extends UniqueSet[F] {

  private[this] val F = Sync[F]

  override def record(value: String, tags: Tag*): F[Unit] =
    F.delay(statsDClient.recordSetValue(aspect, value, (tags ++ defaultTags): _*))
}
