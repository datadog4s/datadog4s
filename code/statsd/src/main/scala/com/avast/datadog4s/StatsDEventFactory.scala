package com.avast.datadog4s

import cats.effect.{ Resource, Sync }
import com.avast.datadog4s.api.EventFactory
import com.timgroup.statsd.NonBlockingStatsDClient

object StatsDEventFactory {
  def make[F[_]: Sync](config: StatsDMetricFactoryConfig): Resource[F, EventFactory[F]] = {
    val F = Sync[F]
    Resource
      .fromAutoCloseable(
        F.delay(
          new NonBlockingStatsDClient(
            "",
            config.statsDServer.getHostName,
            config.statsDServer.getPort,
            config.queueSize
          )
        )
      )
      .map(new statsd.StatsDEventFactory[F](_, config.basePrefix, config))
  }
}
