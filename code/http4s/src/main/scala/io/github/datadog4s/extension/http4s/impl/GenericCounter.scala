package io.github.datadog4s.extension.http4s.impl

import io.github.datadog4s.api.{MetricFactory, Tag}

private[http4s] sealed trait GenericCounter[F[_]] {
  def inc(tags: Tag*): F[Unit]
}

private[http4s] object GenericCounter {
  final class CountWrapper[F[_]](metricFactory: MetricFactory[F], aspect: String) extends GenericCounter[F] {
    private val counter = metricFactory.count(aspect)

    override def inc(tags: Tag*): F[Unit] = counter.inc(tags*)
  }
  final class DistributionCounter[F[_]](metricFactory: MetricFactory[F], aspect: String) extends GenericCounter[F] {
    private val counter = metricFactory.distribution.long(aspect)

    override def inc(tags: Tag*): F[Unit] = counter.record(1L, tags*)
  }
}
