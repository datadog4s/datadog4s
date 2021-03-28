package com.avast.datadog4s.extension.http4s

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.avast.datadog4s.api.{ MetricFactory, Tag }
import com.avast.datadog4s.extension.http4s.DatadogMetricsOps.ClassifierTags
import com.avast.datadog4s.extension.http4s.MetricsOpsBuilder.defaultClassifierTags
import com.avast.datadog4s.extension.http4s.impl.{ ActiveConnections, DefaultMetricsOps }
import org.http4s.metrics.MetricsOps
import cats.syntax.functor._

private[http4s] final class MetricsOpsBuilder[F[_]: Sync](metricFactory: MetricFactory[F]) {
  private[this] var distributionBasedTimers: Boolean = false
  private[this] var classifierTags: ClassifierTags   = defaultClassifierTags

  def useDistributionBasedTimers(): MetricsOpsBuilder[F] = {
    distributionBasedTimers = true
    this
  }

  def useHistogramBasedTimers(): MetricsOpsBuilder[F] = {
    distributionBasedTimers = false
    this
  }

  def setClassifierTags(newClassifierTags: ClassifierTags): MetricsOpsBuilder[F] = {
    classifierTags = newClassifierTags
    this
  }

  def build(): F[MetricsOps[F]] =
    Ref
      .of[F, ActiveConnections](Map.empty)
      .map(new DefaultMetricsOps[F](metricFactory, classifierTags, _, distributionBasedTimers))
}

object MetricsOpsBuilder {
  val defaultClassifierTags: ClassifierTags = classifier => List(Tag.of("classifier", classifier))
}
