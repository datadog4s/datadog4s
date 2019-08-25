package com.avast.datadog4cats.api.metric

import com.avast.datadog4cats.api.Tag

trait UniqueSet[F[_]] {
  def record(value: String, tags: Tag*): F[Unit]
}
