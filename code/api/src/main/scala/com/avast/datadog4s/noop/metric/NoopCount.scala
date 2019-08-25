package com.avast.datadog4s.noop.metric

import cats.Applicative
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.metric.Count

class NoopCount[F[_]: Applicative] extends Count[F] {
  override def modify(delta: Int, tags: Tag*): F[Unit] = Applicative[F].unit
}
