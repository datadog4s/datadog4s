package com.avast.datadog4s

import cats.effect.{ Resource, Sync }
import com.avast.datadog4s.api.MetricFactory
import com.timgroup.statsd.NonBlockingStatsDClientBuilder

object StatsDMetricFactory {
  def make[F[_]: Sync](config: StatsDMetricFactoryConfig): Resource[F, MetricFactory[F]] = {
    val F = Sync[F]
    Resource
      .fromAutoCloseable(
        F.delay(
          new NonBlockingStatsDClientBuilder()
            .hostname(config.statsDServer.getHostName)
            .port(config.statsDServer.getPort)
            .queueSize(config.queueSize)
            .prefix("")
            .build()
        )
      )
      .map(new statsd.StatsDMetricFactory[F](_, config.basePrefix, config.sampleRate, config.defaultTags))
  }
}
