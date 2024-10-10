package io.github.datadog4s.statsd

import cats.effect.{Clock, Sync}
import io.github.datadog4s.api.*
import io.github.datadog4s.api.metric.{Count, Distribution, Gauge, Histogram, Timer, UniqueSet}
import com.timgroup.statsd.StatsDClient as JStatsDClient
import io.github.datadog4s.statsd.metric.{
  CountImpl,
  DistributionDoubleImpl,
  DistributionLongImpl,
  GaugeDoubleImpl,
  GaugeLongImpl,
  HistogramDoubleImpl,
  HistogramLongImpl,
  UniqueSetImpl
}
import io.github.datadog4s.statsd.metric.timer.{DistributionTimer, HistogramTimer}

import java.util.concurrent.TimeUnit

class StatsDMetricFactory[F[_]: Sync](
    statsDClient: JStatsDClient,
    prefix: Option[String],
    defaultSampleRate: Double,
    defaultTags: collection.immutable.Seq[Tag]
) extends MetricFactory[F] {

  private val clock = Clock.create[F]

  private def extendPrefix(ext: String): String = prefix.map(v => s"$v.$ext").getOrElse(ext)

  override val histogram: HistogramFactory[F] = new HistogramFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Long] =
      new HistogramLongImpl[F](
        statsDClient,
        extendPrefix(aspect),
        sampleRate.getOrElse(defaultSampleRate),
        defaultTags
      )

    override def double(aspect: String, sampleRate: Option[Double] = None): Histogram[F, Double] =
      new HistogramDoubleImpl[F](
        statsDClient,
        extendPrefix(aspect),
        sampleRate.getOrElse(defaultSampleRate),
        defaultTags
      )
  }

  override val distribution: DistributionFactory[F] = new DistributionFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double]): Distribution[F, Long] =
      new DistributionLongImpl[F](
        statsDClient,
        extendPrefix(aspect),
        sampleRate.getOrElse(defaultSampleRate),
        defaultTags
      )

    override def double(aspect: String, sampleRate: Option[Double]): Distribution[F, Double] =
      new DistributionDoubleImpl[F](
        statsDClient,
        extendPrefix(aspect),
        sampleRate.getOrElse(defaultSampleRate),
        defaultTags
      )
  }

  override val gauge: GaugeFactory[F] = new GaugeFactory[F] {
    override def long(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Long] =
      new GaugeLongImpl[F](statsDClient, extendPrefix(aspect), sampleRate.getOrElse(defaultSampleRate), defaultTags)

    override def double(aspect: String, sampleRate: Option[Double] = None): Gauge[F, Double] =
      new GaugeDoubleImpl[F](statsDClient, extendPrefix(aspect), sampleRate.getOrElse(defaultSampleRate), defaultTags)
  }

  override val timer: TimerFactory[F] = new TimerFactory[F] {
    override def histogram(
        aspect: String,
        sampleRate: Option[Double] = None,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): Timer[F] = new HistogramTimer[F](
      clock,
      statsDClient,
      extendPrefix(aspect),
      sampleRate.getOrElse(defaultSampleRate),
      defaultTags,
      timeUnit
    )

    override def distribution(
        aspect: String,
        sampleRate: Option[Double] = None,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): Timer[F] = new DistributionTimer[F](
      clock,
      statsDClient,
      extendPrefix(aspect),
      sampleRate.getOrElse(defaultSampleRate),
      defaultTags,
      timeUnit
    )

  }

  override def timer(aspect: String, sampleRate: Option[Double] = None): Timer[F] =
    new HistogramTimer[F](
      clock,
      statsDClient,
      extendPrefix(aspect),
      sampleRate.getOrElse(defaultSampleRate),
      defaultTags,
      TimeUnit.MILLISECONDS
    )

  override def count(aspect: String, sampleRate: Option[Double] = None): Count[F] =
    new CountImpl[F](statsDClient, extendPrefix(aspect), sampleRate.getOrElse(defaultSampleRate), defaultTags)

  override def uniqueSet(aspect: String): UniqueSet[F] =
    new UniqueSetImpl[F](statsDClient, extendPrefix(aspect), defaultTags)

  override def withTags(tags: Tag*): MetricFactory[F] =
    new StatsDMetricFactory[F](statsDClient, prefix, defaultSampleRate, defaultTags ++ tags)

  override def withScope(scope: String): MetricFactory[F] =
    new StatsDMetricFactory[F](statsDClient, Some(extendPrefix(scope)), defaultSampleRate, defaultTags)

}
