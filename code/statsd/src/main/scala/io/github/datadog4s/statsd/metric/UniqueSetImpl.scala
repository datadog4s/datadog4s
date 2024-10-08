package io.github.datadog4s.statsd.metric

import cats.effect.Sync
import com.timgroup.statsd.StatsDClient
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.UniqueSet
import scala.collection.immutable.Seq

class UniqueSetImpl[F[_]: Sync](statsDClient: StatsDClient, aspect: String, defaultTags: Seq[Tag])
    extends UniqueSet[F] {
  private val F = Sync[F]

  override def record(value: String, tags: Tag*): F[Unit] =
    F.delay {
      val finalTags = tags ++ defaultTags
      statsDClient.recordSetValue(aspect, value, finalTags*)
    }
}
