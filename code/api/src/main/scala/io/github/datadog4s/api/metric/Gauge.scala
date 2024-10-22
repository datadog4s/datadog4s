package io.github.datadog4s.api.metric

import io.github.datadog4s.api.Tag

trait Gauge[F[_], N] {
  def set(value: N, tags: Tag*): F[Unit]
}
