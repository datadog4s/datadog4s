package com.avast.cloud.metrics.datadog.api

import scala.language.higherKinds

trait Count[F[_]] {
  def inc(tags: Tag*): F[Unit]
  def modify(delta: Int, tags: Tag*): F[Unit]
  def dec(tags: Tag*): F[Unit]
}


