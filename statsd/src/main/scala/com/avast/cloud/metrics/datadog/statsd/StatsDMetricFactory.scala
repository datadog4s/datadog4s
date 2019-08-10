package com.avast.cloud.metrics.datadog.statsd

import cats.effect.{ Clock, Sync }
import com.avast.cloud.metrics.datadog.StatsDMetricFactoryConfig
import com.avast.cloud.metrics.datadog.api.metric.{ Gauge, Histogram, UniqueSet }
import com.avast.cloud.metrics.datadog.api.{ GaugeFactory, HistogramFactory, MetricFactory, Tag }
import com.avast.cloud.metrics.datadog.statsd.metric._
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

  override def prefixed(addPrefix: String): MetricFactory[F] =
    new StatsDMetricFactory[F](statsDClient, s"$prefix.$addPrefix", config)
}
