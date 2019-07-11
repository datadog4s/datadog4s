package com.avast.cloud.metrics.datadog

import cats.effect.{ Resource, Sync }
import com.avast.cloud.metrics.datadog.api.MetricFactory
import com.avast.cloud.metrics.datadog.impl.MetricFactoryImpl
import com.avast.cloud.metrics.datadog.noop.NoopMetricFactory
import com.timgroup.statsd.NonBlockingStatsDClient

import scala.language.higherKinds

object MetricFactory {

  def noop[F[_]: Sync]: MetricFactory[F] = new NoopMetricFactory[F]

  def make[F[_]: Sync](config: MetricFactoryConfig): Resource[F, MetricFactory[F]] = {
    val F = Sync[F]
    Resource
      .fromAutoCloseable(
        F.delay(
          new NonBlockingStatsDClient(
            config.prefix,
            config.statsDServer.getHostName,
            config.statsDServer.getPort,
            config.defaultTags.map(p => s"${p._1}:${p._2}").toArray: _*
          )
        )
      )
      .map(new MetricFactoryImpl[F](_))
  }
}
