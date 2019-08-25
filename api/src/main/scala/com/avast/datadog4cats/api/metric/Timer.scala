package com.avast.datadog4cats.api.metric

import java.time.Duration

import com.avast.datadog4cats.api.Tag

trait Timer[F[_]] {
  def time[A](f: F[A], tags: Tag*): F[A]

  def record(duration: Duration, tags: Tag*): F[Unit]
}
