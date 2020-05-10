package com.avast.datadog4s

import cats.effect.{ Resource, Sync }
import com.avast.datadog4s.api.EventFactory
import com.timgroup.statsd.NonBlockingStatsDClientBuilder

object StatsDEventFactory {
  def make[F[_]: Sync](config: StatsDMetricFactoryConfig): Resource[F, EventFactory[F]] = {
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
      .map(new statsd.StatsDEventFactory[F](_, config.basePrefix, config))
  }
}
