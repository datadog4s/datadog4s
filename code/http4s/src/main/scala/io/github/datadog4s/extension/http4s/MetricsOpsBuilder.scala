package io.github.datadog4s.extension.http4s

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.functor.*
import io.github.datadog4s.api.{MetricFactory, Tag}
import io.github.datadog4s.extension.http4s.DatadogMetricsOps.ClassifierTags
import io.github.datadog4s.extension.http4s.impl.{ActiveConnections, DefaultMetricsOps}
import org.http4s.metrics.MetricsOps

import scala.annotation.nowarn

@nowarn final case class MetricsOpsBuilder[F[_]: Sync] private (
    metricFactory: MetricFactory[F],
    distributionBasedTimers: Boolean,
    distributionBasedCounters: Boolean,
    classifierTags: ClassifierTags
) {

  /** Force MetricOps to use [[TimerFactory.distribution TimerFactory.distribution]] for timing http4s requests. For the
    * implications please see [[TimerFactory.distribution TimerFactory.distribution]] scaladoc.
    */
  def useDistributionBasedTimers(): MetricsOpsBuilder[F] =
    copy(distributionBasedTimers = true)

  /** Force MetricOps to use [[TimerFactory.histogram TimerFactory.histogram]] for timing http4s requests. For the
    * implications please see [[TimerFactory.histogram TimerFactory.histogram]] scaladoc.
    */
  def useHistogramBasedTimers(): MetricsOpsBuilder[F] =
    copy(distributionBasedTimers = false)

  /** Force MetricOps to use [[DistributionFactory DistributionFactory]] for counting http4s requests. This is useful in
    * serverless models like AWS Lambda to aggregate all counter values. For more info see
    * [[https://docs.datadoghq.com/serverless/custom_metrics/#understanding-distribution-metrics Datadog documentation]].
    */
  def useDistributionBasedCounters(): MetricsOpsBuilder[F] =
    copy(distributionBasedCounters = true)

  /** Force MetricOps to use [[Count Count]] for counting http4s requests. This is sufficient for requests on servers
    * where the host is automatically added as a tag. For more info see
    * [[https://docs.datadoghq.com/serverless/custom_metrics/#understanding-distribution-metrics Datadog documentation]].
    */
  def useHistogramBasedCounters(): MetricsOpsBuilder[F] =
    copy(distributionBasedCounters = false)

  /** Function for computing tags based on provided classifier. By default uses
    * [[MetricsOpsBuilder.defaultClassifierTags]]
    */
  def setClassifierTags(newClassifierTags: ClassifierTags): MetricsOpsBuilder[F] =
    copy(classifierTags = newClassifierTags)

  def build(): F[MetricsOps[F]] =
    Ref
      .of[F, ActiveConnections](Map.empty)
      .map(
        new DefaultMetricsOps[F](metricFactory, classifierTags, _, distributionBasedTimers, distributionBasedCounters)
      )
}

object MetricsOpsBuilder {
  val defaultClassifierTags: ClassifierTags = classifier => List(Tag.of("classifier", classifier))

  def withDefaults[F[_]: Sync](metricFactory: MetricFactory[F]): MetricsOpsBuilder[F] =
    new MetricsOpsBuilder[F](
      metricFactory,
      distributionBasedTimers = false,
      distributionBasedCounters = false,
      classifierTags = defaultClassifierTags
    )
}
