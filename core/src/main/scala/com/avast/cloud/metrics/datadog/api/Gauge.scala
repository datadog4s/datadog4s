package com.avast.cloud.metrics.datadog.api

import scala.language.higherKinds

trait Gauge[F[_]] {
  def set(value: Long, tags: Tag*): F[Unit]
  def set(value: Double, tags: Tag*): F[Unit]
}
