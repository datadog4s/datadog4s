package com.avast.datadog4s.statsd

import cats.effect.{ Clock, Sync }
import com.avast.datadog4s.StatsDMetricFactoryConfig
import com.avast.datadog4s.api.metric.{ Eventer, Gauge, Histogram, UniqueSet }
import com.avast.datadog4s.api.{ GaugeFactory, HistogramFactory, MetricFactory, Tag }
import com.avast.datadog4s.statsd.metric._
import com.timgroup.statsd.StatsDClient

class StatsDMetricFactory[F[_]: Sync](statsDClient: StatsDClient, basePrefix: String, config: StatsDMetricFactoryConfig)
    extends MetricFactory[F] {
  import config.{ defaultTags, sampleRate => defaultSampleRate }

  private[this] val clock = Clock.create[F]

  override val histogram: HistogramFactory[F] = new HistogramFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Long] =
      new HistogramLongImpl[F](
        statsDClient,
        s"$basePrefix.$aspect",
        sampleRate.getOrElse(defaultSampleRate),
        defaultTags
      )

    override def double(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Double] =
      new HistogramDoubleImpl[F](
        statsDClient,
        s"$basePrefix.$aspect",
        sampleRate.getOrElse(defaultSampleRate),
        defaultTags
      )
  }

  override val gauge: GaugeFactory[F] = new GaugeFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Long] =
      new GaugeLongImpl[F](statsDClient, s"$basePrefix.$aspect", sampleRate.getOrElse(defaultSampleRate), defaultTags)

    override def double(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Double] =
      new GaugeDoubleImpl[F](statsDClient, s"$basePrefix.$aspect", sampleRate.getOrElse(defaultSampleRate), defaultTags)
  }

  override def timer(aspect: String, sampleRate: Option[Double] = None) =
    new TimerImpl[F](
      clock,
      statsDClient,
      s"$basePrefix.$aspect",
      sampleRate.getOrElse(defaultSampleRate),
      defaultTags
    )

  override def count(aspect: String, sampleRate: Option[Double] = None) =
    new CountImpl[F](statsDClient, s"$basePrefix.$aspect", sampleRate.getOrElse(defaultSampleRate), defaultTags)

  override def uniqueSet(aspect: String): UniqueSet[F] =
    new UniqueSetImpl[F](statsDClient, s"$basePrefix.$aspect", defaultTags)

  override def eventer: Eventer[F] = new EventerImpl[F](statsDClient, defaultTags)

  override def withTags(tags: Tag*): MetricFactory[F] =
    new StatsDMetricFactory[F](statsDClient, basePrefix, config.copy(defaultTags = config.defaultTags ++ tags))

  override def withScope(scope: String): MetricFactory[F] =
    new StatsDMetricFactory[F](statsDClient, s"$basePrefix.$scope", config)
}
