package com.avast.cloud.metrics.datadog.statsd

import cats.effect.{ Clock, Sync }
import com.avast.cloud.metrics.datadog.api.metric.{ Gauge, Histogram, UniqueSet }
import com.avast.cloud.metrics.datadog.api.{ GaugeFactory, HistogramFactory, MetricFactory }
import com.avast.cloud.metrics.datadog.statsd.metric.{
  CountImpl,
  GaugeDoubleImpl,
  GaugeLongImpl,
  HistogramDoubleImpl,
  HistogramLongImpl,
  TimerImpl,
  UniqueSetImpl
}
import com.timgroup.statsd.StatsDClient

class MetricFactoryImpl[F[_]: Sync](statsDClient: StatsDClient) extends MetricFactory[F] {
  private[this] val clock = Clock.create[F]

  override val histogram: HistogramFactory[F] = new HistogramFactory[F] {
    override def long(aspect: String, sampleRate: Double): Histogram[F, Long] =
      new HistogramLongImpl[F](statsDClient, aspect, sampleRate)

    override def double(aspect: String, sampleRate: Double): Histogram[F, Double] =
      new HistogramDoubleImpl[F](statsDClient, aspect, sampleRate)
  }

  override val gauge: GaugeFactory[F] = new GaugeFactory[F] {
    override def long(aspect: String, sampleRate: Double): Gauge[F, Long] =
      new GaugeLongImpl[F](statsDClient, aspect, sampleRate)

    override def double(aspect: String, sampleRate: Double): Gauge[F, Double] =
      new GaugeDoubleImpl[F](statsDClient, aspect, sampleRate)
  }

  override def timer(prefix: String, sampleRate: Double = 1.0) =
    new TimerImpl[F](clock, statsDClient, prefix, sampleRate)

  override def count(prefix: String, sampleRate: Double = 1.0) =
    new CountImpl[F](statsDClient, prefix, sampleRate)

  override def uniqueSet(aspect: String): UniqueSet[F] =
    new UniqueSetImpl[F](statsDClient, aspect)

}
