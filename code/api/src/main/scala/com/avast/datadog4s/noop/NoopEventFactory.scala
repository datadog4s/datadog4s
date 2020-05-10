package com.avast.datadog4s.noop

import cats.Applicative
import com.avast.datadog4s.api.event.Eventer
import com.avast.datadog4s.api.{ EventFactory, Tag }
import com.avast.datadog4s.noop.event.NoopEventer

class NoopEventFactory[F[_]: Applicative] extends EventFactory[F] {
  override def eventer: Eventer[F] = new NoopEventer[F]

  override def withTags(tags: Tag*): EventFactory[F] = this

  override def withScope(prefix: String): EventFactory[F] = this
}
