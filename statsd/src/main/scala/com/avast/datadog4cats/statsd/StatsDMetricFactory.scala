package com.avast.datadog4cats.statsd

import cats.effect.{ Clock, Sync }
import com.avast.datadog4cats.StatsDMetricFactoryConfig
import com.avast.datadog4cats.api.metric.{ Gauge, Histogram, UniqueSet }
import com.avast.datadog4cats.api.{ GaugeFactory, HistogramFactory, MetricFactory, Tag }
import com.avast.datadog4cats.statsd.metric._
import com.timgroup.statsd.StatsDClient

class StatsDMetricFactory[F[_]: Sync](statsDClient: StatsDClient, prefix: String, config: StatsDMetricFactoryConfig)
    extends MetricFactory[F] {

  import config.{ defaultTags, sampleRate => defaultSampleRate }

  private[this] val clock = Clock.create[F]

  override val histogram: HistogramFactory[F] = new HistogramFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Long] =
      new HistogramLongImpl[F](statsDClient, aspect, sampleRate.getOrElse(defaultSampleRate), defaultTags)

    override def double(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Double] =
      new HistogramDoubleImpl[F](statsDClient, aspect, sampleRate.getOrElse(defaultSampleRate), defaultTags)
  }

  override val gauge: GaugeFactory[F] = new GaugeFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Long] =
      new GaugeLongImpl[F](statsDClient, aspect, sampleRate.getOrElse(defaultSampleRate), defaultTags)

    override def double(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Double] =
      new GaugeDoubleImpl[F](statsDClient, aspect, sampleRate.getOrElse(defaultSampleRate), defaultTags)
  }

  override def timer(prefix: String, sampleRate: Option[Double] = None) =
    new TimerImpl[F](
      clock,
      statsDClient,
      prefix,
      sampleRate.getOrElse(defaultSampleRate),
      defaultTags
    )

  override def count(prefix: String, sampleRate: Option[Double] = None) =
    new CountImpl[F](statsDClient, prefix, sampleRate.getOrElse(defaultSampleRate), defaultTags)

  override def uniqueSet(aspect: String): UniqueSet[F] =
    new UniqueSetImpl[F](statsDClient, aspect, defaultTags)

  override def withTags(tags: Tag*): MetricFactory[F] =
    new StatsDMetricFactory[F](statsDClient, prefix, config.copy(defaultTags = config.defaultTags ++ tags))

  override def withScope(scope: String): MetricFactory[F] =
    new StatsDMetricFactory[F](statsDClient, s"$prefix.$scope", config)
}
