package com.avast.cloud.metrics.datadog.impl

import cats.effect.{ Clock, Sync }
import com.avast.cloud.metrics.datadog.api.{ Gauge, Histogram, MetricFactory, UniqueSet }
import com.timgroup.statsd.StatsDClient

import scala.language.higherKinds

class MetricFactoryImpl[F[_]: Sync](statsDClient: StatsDClient) extends MetricFactory[F] {
  private[this] val clock = Clock.create[F]

  override def timer(prefix: String, sampleRate: Double = 1.0) =
    new TimerImpl[F](clock, statsDClient, prefix, sampleRate)

  override def count(prefix: String, sampleRate: Double = 1.0) =
    new CountImpl[F](statsDClient, prefix, sampleRate)

  override def uniqueSet(aspect: String): UniqueSet[F] = 
    new UniqueSetImpl[F](statsDClient, aspect)

  override def histogram(aspect: String, sampleRate: Double = 1.0): Histogram[F] =
    new HistogramImpl[F](statsDClient, aspect, sampleRate)
  
  override def gauge(aspect: String, sampleRate: Double = 1.0): Gauge[F] =
    new GaugeImpl[F](statsDClient, aspect, sampleRate)
}
