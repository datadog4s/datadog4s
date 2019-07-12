package com.avast.cloud.metrics.datadog.api.metric

import com.avast.cloud.metrics.datadog.api.Tag

trait UniqueSet[F[_]] {
  def record(value: String, tags: Tag*): F[Unit]
}
