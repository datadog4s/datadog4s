package com.avast.cloud.metrics.datadog.api

trait UniqueSet[F[_]] {
  def record(value: String, tags: Tag*): F[Unit]
}
