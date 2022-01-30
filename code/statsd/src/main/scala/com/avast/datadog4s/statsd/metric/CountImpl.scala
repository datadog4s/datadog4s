package com.avast.datadog4s.statsd.metric

import cats.effect.Sync
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Count
import com.timgroup.statsd.StatsDClient
import scala.collection.immutable.Seq

class CountImpl[F[_]: Sync](statsDClient: StatsDClient, prefix: String, sampleRate: Double, defaultTags: Seq[Tag])
    extends Count[F] {
  private val F = Sync[F]

  override def modify(delta: Int, tags: Tag*): F[Unit] =
    F.delay(statsDClient.count(prefix, delta.toLong, sampleRate, (tags ++ defaultTags)*))
}
