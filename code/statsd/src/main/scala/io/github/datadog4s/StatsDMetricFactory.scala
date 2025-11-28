package io.github.datadog4s

import cats.effect.{Resource, Sync}
import io.github.datadog4s.api.MetricFactory
import io.github.datadog4s.statsd.StatsDClient

object StatsDMetricFactory {
  def make[F[_]: Sync](config: StatsDMetricFactoryConfig): Resource[F, MetricFactory[F]] =
    StatsDClient
      .make(config.statsDServer, config.queueSize)
      .map(fromClient(_, config))

  def fromClient[F[_]: Sync](
      client: com.timgroup.statsd.StatsDClient,
      config: StatsDMetricFactoryConfig
  ): MetricFactory[F] =
    new statsd.StatsDMetricFactory[F](client, config.basePrefix, config.sampleRate, config.defaultTags)
}
