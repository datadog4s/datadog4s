package com.avast.datadog4s.statsd.metric

import cats.effect.Sync
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.UniqueSet
import com.timgroup.statsd.StatsDClient
import scala.collection.immutable.Seq

class UniqueSetImpl[F[_]: Sync](statsDClient: StatsDClient, aspect: String, defaultTags: Seq[Tag])
    extends UniqueSet[F] {
  private val F = Sync[F]

  override def record(value: String, tags: Tag*): F[Unit] =
    F.delay(statsDClient.recordSetValue(aspect, value, (tags ++ defaultTags)*))
}
