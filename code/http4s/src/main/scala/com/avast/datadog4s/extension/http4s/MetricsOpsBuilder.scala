package com.avast.datadog4s.extension.http4s
import cats.effect.{Ref, Sync}
import cats.syntax.functor.*
import com.avast.datadog4s.api.{MetricFactory, Tag}
import com.avast.datadog4s.extension.http4s.DatadogMetricsOps.ClassifierTags
import com.avast.datadog4s.extension.http4s.impl.{ActiveConnections, DefaultMetricsOps}
import org.http4s.metrics.MetricsOps

final case class MetricsOpsBuilder[F[_]: Sync] private (
    metricFactory: MetricFactory[F],
    distributionBasedTimers: Boolean,
    classifierTags: ClassifierTags
) {

  /** Force MetricOps to use [[com.avast.datadog4s.api.TimerFactory.distribution TimerFactory.distribution]] for timing http4s requests.
    * For the implications please see [[com.avast.datadog4s.api.TimerFactory.distribution TimerFactory.distribution]] scaladoc.
    */
  def useDistributionBasedTimers(): MetricsOpsBuilder[F] =
    copy(distributionBasedTimers = true)

  /** Force MetricOps to use [[com.avast.datadog4s.api.TimerFactory.histogram TimerFactory.histogram]] for timing http4s requests.
    * For the implications please see [[com.avast.datadog4s.api.TimerFactory.histogram TimerFactory.histogram]] scaladoc.
    */
  def useHistogramBasedTimers(): MetricsOpsBuilder[F] =
    copy(distributionBasedTimers = false)

  /** Function for computing tags based on provided classifier. By default uses [[MetricsOpsBuilder.defaultClassifierTags]]
    */
  def setClassifierTags(newClassifierTags: ClassifierTags): MetricsOpsBuilder[F] =
    copy(classifierTags = newClassifierTags)

  def build(): F[MetricsOps[F]] =
    Ref
      .of[F, ActiveConnections](Map.empty)
      .map(new DefaultMetricsOps[F](metricFactory, classifierTags, _, distributionBasedTimers))
}

object MetricsOpsBuilder {
  val defaultClassifierTags: ClassifierTags = classifier => List(Tag.of("classifier", classifier))

  def withDefaults[F[_]: Sync](metricFactory: MetricFactory[F]): MetricsOpsBuilder[F] =
    new MetricsOpsBuilder[F](metricFactory, distributionBasedTimers = false, classifierTags = defaultClassifierTags)
}
