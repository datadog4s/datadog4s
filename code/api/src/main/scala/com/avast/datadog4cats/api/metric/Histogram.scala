package com.avast.datadog4cats.api.metric

import com.avast.datadog4cats.api.Tag

trait Histogram[F[_], N] {
  def record(value: N, tags: Tag*): F[Unit]
}
