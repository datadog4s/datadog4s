package com.avast.cloud.metrics.datadog.api

import scala.language.higherKinds

trait Timer[F[_]] {
  def time[A](f: F[A], tags: Tag*): F[A]

  def recordExecutionTime(timeInMs: Long, tags: Tag*): F[Unit]
}
