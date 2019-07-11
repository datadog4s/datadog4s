package com.avast.cloud.metrics.datadog.api

import scala.language.higherKinds

trait UniqueSet[F[_]] {
  def record(value: String, tags: Tag*): F[Unit]
}
