package com.avast.datadog4s

import cats.effect.{ Resource, Sync }
import com.avast.datadog4s.api.MetricFactory
import com.avast.datadog4s.statsd.StatsDClient

object StatsDMetricFactory {
  def make[F[_]: Sync](config: StatsDMetricFactoryConfig): Resource[F, MetricFactory[F]] =
    StatsDClient
      .make(config.statsDServer, config.queueSize)
      .map(new statsd.StatsDMetricFactory[F](_, config.basePrefix, config.sampleRate, config.defaultTags))
}
