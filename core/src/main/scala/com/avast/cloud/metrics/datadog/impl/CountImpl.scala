package com.avast.cloud.metrics.datadog.impl

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.{ Count, Tag }
import com.timgroup.statsd.StatsDClient

import scala.language.higherKinds

class CountImpl[F[_]: Sync](statsDClient: StatsDClient, prefix: String, sampleRate: Double) extends Count[F] {
  private[this] val F = Sync[F]

  override def inc(tags: Tag*): F[Unit] = F.delay(statsDClient.increment(prefix, sampleRate, tags: _*))

  override def modify(delta: Int, tags: Tag*): F[Unit] =
    F.delay(statsDClient.count(prefix, delta, sampleRate, tags: _*))

  override def dec(tags: Tag*): F[Unit] = F.delay(statsDClient.decrement(prefix, sampleRate, tags: _*))

}
