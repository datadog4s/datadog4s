package com.avast.cloud.metrics.datadog.api.metric

import com.avast.cloud.metrics.datadog.api.Tag

trait Gauge[F[_], N] {
  def set(value: N, tags: Tag*): F[Unit]
}
