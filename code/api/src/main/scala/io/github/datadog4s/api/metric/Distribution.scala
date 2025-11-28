package io.github.datadog4s.api.metric

import io.github.datadog4s.api.Tag

trait Distribution[F[_], N] {
  def record(value: N, tags: Tag*): F[Unit]
}
