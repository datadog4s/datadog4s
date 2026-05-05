package io.github.datadog4s.api

import io.github.datadog4s.api.metric.Timer
import java.util.concurrent.TimeUnit

trait TimerFactory[F[_]] {

  /** Create histogram-backed timer. The implications are that this the histograms are computed in datadog agent and not
    * on DDog servers. This means that the data in your dashboards etc might be less precise as they will be computed by
    * aggregating histograms. For more info see
    * [[https://docs.datadoghq.com/dashboards/guide/how-to-graph-percentiles-in-datadog/#local-aggregations Datadog documentation]].
    *
    * In general, [[TimerFactory.distribution]] is probably preferred.
    */
  def histogram(aspect: String, sampleRate: Option[Double] = None, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): Timer[F]

  /** Create distribution-based timer. Unlike [[TimerFactory.histogram]], this implementation will aggregate and compute
    * histograms on datadog servers which means that you will have statistically correct data.
    *
    * For more information about distribution see
    * [[https://docs.datadoghq.com/metrics/distributions/ Datadog documentation]]
    */
  def distribution(
      aspect: String,
      sampleRate: Option[Double] = None,
      timeUnit: TimeUnit = TimeUnit.MILLISECONDS
  ): Timer[F]
}
