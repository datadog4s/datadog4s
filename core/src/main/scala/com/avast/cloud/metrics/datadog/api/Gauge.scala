package com.avast.cloud.metrics.datadog.api

import scala.language.higherKinds

trait Gauge[F[_]] {
  def setTo(value: Long, tags: Tag*): F[Unit]
  def setTo(value: Double, tags: Tag*): F[Unit]
}
