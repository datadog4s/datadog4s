package com.avast.datadog4s.api.event

import com.avast.datadog4s.api.Tag

trait Eventer[F[_]] {
  def send(
    event: Event,
    tags: Tag*
  ): F[Unit]
}
