package com.avast.datadog4s

import cats.effect.{ Resource, Sync }
import com.avast.datadog4s.api.MetricFactory
import com.avast.datadog4s.statsd.StatsDMetricFactory
import com.timgroup.statsd.NonBlockingStatsDClient

object StatsDMetricFactory {

  def make[F[_]: Sync](config: StatsDMetricFactoryConfig): Resource[F, MetricFactory[F]] = {
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
      .map(new StatsDMetricFactory[F](_, config.basePrefix, config))
  }
}
