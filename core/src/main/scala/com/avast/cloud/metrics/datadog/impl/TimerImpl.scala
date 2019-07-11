package com.avast.cloud.metrics.datadog.impl

import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.{ Tag, Timer }
import com.timgroup.statsd.StatsDClient

import scala.language.higherKinds

class TimerImpl[F[_]: Sync](statsDClient: StatsDClient, prefix: String, sampleRate: Double) extends Timer[F] {
  override def time[A](f: F[A], tags: Tag*): F[A] = ???

  override def registerTime(nanos: Double, tags: Tag*): F[Unit] = ???
}
