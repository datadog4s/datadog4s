package com.avast.cloud.metrics.datadog.noop.metric

import cats.Applicative
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.Count

class NoopCount[F[_]: Applicative] extends Count[F] {
  override def modify(delta: Int, tags: Tag*): F[Unit] = Applicative[F].unit
}
