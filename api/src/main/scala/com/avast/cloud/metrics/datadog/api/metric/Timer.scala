package com.avast.cloud.metrics.datadog.api.metric

import java.time.Duration

import com.avast.cloud.metrics.datadog.api.Tag

trait Timer[F[_]] {
  def time[A](f: F[A], tags: Tag*): F[A]

  def recordExecutionTime(duration: Duration, tags: Tag*): F[Unit]
}
