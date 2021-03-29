package com.avast.datadog4s.extension.http4s

import cats.effect.Sync
import cats.syntax.functor._
import com.avast.datadog4s.api.{ MetricFactory, Tag }
import com.avast.datadog4s.extension.http4s.impl.{ ActiveConnections, DefaultMetricsOps }
import org.http4s.metrics.MetricsOps
import cats.effect.Ref

object DatadogMetricsOps {
  type ClassifierTags = String => List[Tag]

  val defaultClassifierTags: ClassifierTags = classifier => List(Tag.of("classifier", classifier))

  def make[F[_]](metricFactory: MetricFactory[F], classifierTags: ClassifierTags = defaultClassifierTags)(implicit
    F: Sync[F]
  ): F[MetricsOps[F]] =
    Ref.of[F, ActiveConnections](Map.empty).map(new DefaultMetricsOps[F](metricFactory, classifierTags, _))

}
