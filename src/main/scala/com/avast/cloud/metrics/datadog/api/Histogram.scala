package com.avast.cloud.metrics.datadog.api

import scala.language.higherKinds

trait Histogram[F[_]] {
  def record(value: Long, tags: Tag*): F[Unit]
  def record[A: Numeric](value: Double, tags: Tag*): F[Unit]
}
