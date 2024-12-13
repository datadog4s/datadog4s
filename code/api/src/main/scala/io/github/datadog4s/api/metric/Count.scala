package io.github.datadog4s.api.metric

import io.github.datadog4s.api.Tag

trait Count[F[_]] {
  def modify(delta: Int, tags: Tag*): F[Unit]
  def inc(tags: Tag*): F[Unit] = modify(1, tags*)
  def dec(tags: Tag*): F[Unit] = modify(-1, tags*)
}
