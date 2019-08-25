package com.avast.datadog4s.api.metric

import com.avast.datadog4s.api.Tag

trait UniqueSet[F[_]] {
  def record(value: String, tags: Tag*): F[Unit]
}
