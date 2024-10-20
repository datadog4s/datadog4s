package io.github.datadog4s.api.metric

import io.github.datadog4s.api.Tag

trait UniqueSet[F[_]] {
  def record(value: String, tags: Tag*): F[Unit]
}
