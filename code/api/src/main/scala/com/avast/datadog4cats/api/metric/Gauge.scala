package com.avast.datadog4cats.api.metric

import com.avast.datadog4cats.api.Tag

trait Gauge[F[_], N] {
  def set(value: N, tags: Tag*): F[Unit]
}
