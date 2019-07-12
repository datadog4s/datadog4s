package com.avast.cloud.metrics.datadog.api.metric

import com.avast.cloud.metrics.datadog.api.Tag

trait Count[F[_]] {
  def modify(delta: Int, tags: Tag*): F[Unit]
  def inc(tags: Tag*): F[Unit] = modify(1, tags: _*)
  def dec(tags: Tag*): F[Unit] = modify(-1, tags: _*)
}
