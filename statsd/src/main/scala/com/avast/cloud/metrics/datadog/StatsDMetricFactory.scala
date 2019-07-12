package com.avast.cloud.metrics.datadog

import cats.effect.{Resource, Sync}
import com.avast.cloud.metrics.datadog.api.{MetricFactory, Tag}
import com.avast.cloud.metrics.datadog.statsd.MetricFactoryImpl
import com.timgroup.statsd.NonBlockingStatsDClient

object StatsDMetricFactory {

  def make[F[_]: Sync](config: MetricFactoryConfig): Resource[F, MetricFactory[F]] = {
    val F = Sync[F]
    Resource
      .fromAutoCloseable(
        F.delay(
          new NonBlockingStatsDClient(
            config.prefix,
            config.statsDServer.getHostName,
            config.statsDServer.getPort
          )
        )
      )
      .map(new MetricFactoryImpl[F](_, config.sampleRate, config.defaultTags.map(p => Tag.of(p._1, p._2)).toVector))
  }
}
