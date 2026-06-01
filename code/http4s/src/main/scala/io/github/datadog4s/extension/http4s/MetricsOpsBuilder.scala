package io.github.datadog4s.extension.http4s

import cats.effect.{Ref, Sync}
import cats.syntax.functor.*
import io.github.datadog4s.api.{MetricFactory, Tag}
import DatadogMetricsOps.ClassifierTags
import impl.{ActiveConnections, DefaultMetricsOps}
import org.http4s.metrics.MetricsOps

import scala.annotation.nowarn

@nowarn final case class MetricsOpsBuilder[F[_]: Sync] private (
    metricFactory: MetricFactory[F],
    distributionBasedTimers: Boolean,
    distributionBasedCounters: Boolean,
    distributionBasedActiveRequests: Boolean = false,
    classifierTags: ClassifierTags
) {

  /** Force MetricOps to use [[io.github.datadog4s.api.TimerFactory.distribution TimerFactory.distribution]] for timing
    * http4s requests. For the implications please see
    * [[io.github.datadog4s.api.TimerFactory.distribution TimerFactory.distribution]] scaladoc.
    */
  def useDistributionBasedTimers(): MetricsOpsBuilder[F] =
    copy(distributionBasedTimers = true)

  /** Force MetricOps to use [[io.github.datadog4s.api.TimerFactory.histogram TimerFactory.histogram]] for timing http4s
    * requests. For the implications please see
    * [[io.github.datadog4s.api.TimerFactory.histogram TimerFactory.histogram]] scaladoc.
    */
  def useHistogramBasedTimers(): MetricsOpsBuilder[F] =
    copy(distributionBasedTimers = false)

  /** Force MetricOps to use [[io.github.datadog4s.api.DistributionFactory DistributionFactory]] for counting http4s
    * requests. This is useful in serverless models like AWS Lambda to aggregate all counter values. For more info see
    * [[https://docs.datadoghq.com/serverless/custom_metrics/#understanding-distribution-metrics Datadog documentation]].
    */
  def useDistributionBasedCounters(): MetricsOpsBuilder[F] =
    copy(distributionBasedCounters = true)

  /** Force MetricOps to use [[io.github.datadog4s.api.metric.Count Count]] for counting http4s requests. This is
    * sufficient for requests on servers where the host is automatically added as a tag. For more info see
    * [[https://docs.datadoghq.com/serverless/custom_metrics/#understanding-distribution-metrics Datadog documentation]].
    */
  def useHistogramBasedCounters(): MetricsOpsBuilder[F] =
    copy(distributionBasedCounters = false)

  /** Force MetricOps to use [[io.github.datadog4s.api.DistributionFactory DistributionFactory]] for tracking
    * active_requests. This preserves intra-flush peaks so spikes that resolve within the StatsD flush interval (default
    * 10s) are visible in Datadog.
    *
    * '''Breaking change for existing dashboards''': switching metric type will break existing dashboards and monitors
    * querying active_requests as a gauge — update those queries to use the `max` aggregation after enabling this option.
    *
    * '''Idle-period gaps''': unlike a gauge (which the StatsD agent re-emits every flush even when unchanged), a
    * distribution only emits data when a request arrives or completes. During flush intervals with zero active requests,
    * no data point is sent and Datadog will show a gap. Monitors configured to alert on "no data" may fire spuriously
    * on idle or low-traffic services.
    */
  def useDistributionBasedActiveRequests(): MetricsOpsBuilder[F] =
    copy(distributionBasedActiveRequests = true)

  /** Force MetricOps to use a gauge for tracking active_requests. Only the last value in each StatsD flush interval is
    * sent to Datadog, so intra-flush peaks may be lost. This is the default.
    */
  def useGaugeBasedActiveRequests(): MetricsOpsBuilder[F] =
    copy(distributionBasedActiveRequests = false)

  /** Function for computing tags based on provided classifier. By default uses
    * [[MetricsOpsBuilder.defaultClassifierTags]]
    */
  def setClassifierTags(newClassifierTags: ClassifierTags): MetricsOpsBuilder[F] =
    copy(classifierTags = newClassifierTags)

  def build(): F[MetricsOps[F]] =
    Ref
      .of[F, ActiveConnections](Map.empty)
      .map(
        new DefaultMetricsOps[F](
          metricFactory,
          classifierTags,
          _,
          distributionBasedTimers,
          distributionBasedCounters,
          distributionBasedActiveRequests
        )
      )
}

object MetricsOpsBuilder {
  val defaultClassifierTags: ClassifierTags = classifier => List(Tag.of("classifier", classifier))

  def withDefaults[F[_]: Sync](metricFactory: MetricFactory[F]): MetricsOpsBuilder[F] =
    new MetricsOpsBuilder[F](
      metricFactory,
      distributionBasedTimers = false,
      distributionBasedCounters = false,
      distributionBasedActiveRequests = false,
      classifierTags = defaultClassifierTags
    )
}
