package com.avast.cloud.metrics.datadog.api.metric

import com.avast.cloud.metrics.datadog.api.Tag

trait Histogram[F[_], N] {
  def record(value: N, tags: Tag*): F[Unit]
}
