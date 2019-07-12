package com.avast.cloud.metrics.datadog.noop.metric

import cats.Applicative
import com.avast.cloud.metrics.datadog.api.Tag
import com.avast.cloud.metrics.datadog.api.metric.UniqueSet

class NoopUniqueSet[F[_]: Applicative] extends UniqueSet[F] {
  override def record(value: String, tags: Tag*): F[Unit] = Applicative[F].unit
}
