package com.avast.cloud.metrics.datadog

import cats.effect.{ Resource, Sync }
import com.avast.cloud.metrics.datadog.api.{ MetricFactory, Tag }
import com.avast.cloud.metrics.datadog.statsd.StatsDMetricFactory
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
