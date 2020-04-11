package com.avast.datadog4s.noop.metric

import cats.Applicative
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.event.Event
import com.avast.datadog4s.api.metric.Eventer

class NoopEventer[F[_]: Applicative] extends Eventer[F] {

  override def send(event: Event, tags: Tag*): F[Unit] = Applicative[F].unit

}
