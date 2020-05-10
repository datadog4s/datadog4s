package com.avast.datadog4s.api

import com.avast.datadog4s.api.event.Eventer

trait EventFactory[F[_]] {
  def eventer: Eventer[F]

  def withTags(tags: Tag*): EventFactory[F]
  def withScope(name: String): EventFactory[F]
}
