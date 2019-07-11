package com.avast.cloud.metrics.datadog

import cats.effect.{Resource, Sync}
import com.avast.cloud.metrics.datadog.impl.{CountImpl, TimerImpl}
import com.timgroup.statsd.{NonBlockingStatsDClient, StatsDClient}

class MetricFactory[F: Sync](statsDClient: StatsDClient) {
  def timer(prefix: String, sampleRate: Double = 1.0) =
    new TimerImpl[F](statsDClient, prefix, sampleRate)
  def count(prefix: String, sampleRate: Double = 1.0) =
    new CountImpl[F](statsDClient, prefix, sampleRate)
}

object MetricFactory {

  def make[F[_]: Sync](
      config: MetricFactoryConfig): Resource[F, F[MetricFactory[F]]] = {
    val F = Sync[F]
    Resource
      .fromAutoCloseable(
        F.delay(
          new NonBlockingStatsDClient(
            config.prefix,
            config.statsDServer.getHostName,
            config.statsDServer.getPort,
            config.defaultTags.map(p => s"${p._1}:${p._2}").toArray: _*)))
      .map(new MetricFactory[F](_))
  }
}
