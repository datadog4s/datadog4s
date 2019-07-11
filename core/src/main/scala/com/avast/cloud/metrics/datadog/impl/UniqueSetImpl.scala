package com.avast.cloud.metrics.datadog.impl

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.{ Tag, UniqueSet }
import com.timgroup.statsd.StatsDClient

import scala.language.higherKinds

class UniqueSetImpl[F[_]: Sync](statsDClient: StatsDClient, aspect: String) extends UniqueSet[F] {
  
  private[this] val F = Sync[F]
  
  override def record(value: String, tags: Tag*): F[Unit] =
    F.delay(statsDClient.recordSetValue(aspect, value, tags: _*))
}
