package io.github.datadog4s.statsd.metric

import cats.effect.Sync
import com.timgroup.statsd.StatsDClient
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.Count
import scala.collection.immutable.Seq

class CountImpl[F[_]: Sync](statsDClient: StatsDClient, prefix: String, sampleRate: Double, defaultTags: Seq[Tag])
    extends Count[F] {
  private val F = Sync[F]

  override def modify(delta: Int, tags: Tag*): F[Unit] = F.delay {
    val finalTags = tags ++ defaultTags
    statsDClient.count(prefix, delta.toLong, sampleRate, finalTags*)
  }
}
