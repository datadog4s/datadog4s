package com.avast.datadog4s

import cats.effect.{ Resource, Sync }
import com.avast.datadog4s.api.MetricFactory
import com.avast.datadog4s.statsd.StatsDClient

object StatsDMetricFactory {
  def makeResource[F[_]: Sync](config: StatsDMetricFactoryConfig): Resource[F, MetricFactory[F]] =
    StatsDClient
      .makeResource(config.statsDServer, config.queueSize)
      .map(new statsd.StatsDMetricFactory[F](_, config.basePrefix, config.sampleRate, config.defaultTags))

  def makeUnsafe[F[_]: Sync](config: StatsDMetricFactoryConfig): MetricFactory[F] = {
    val client = StatsDClient.makeUnsafe(config.statsDServer, config.queueSize)
    new statsd.StatsDMetricFactory[F](client, config.basePrefix, config.sampleRate, config.defaultTags)
  }
}
