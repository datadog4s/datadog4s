package com.avast.cloud.datadog4s.inmemory

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import com.avast.datadog4s.api.event.{ Event, Eventer }
import com.avast.datadog4s.api.{ EventFactory, Tag }

class MockEventFactory[F[_]: Sync](val state: Ref[F, Map[String, Vector[Record[Any]]]]) extends EventFactory[F] {

  private def updateState[A](aspect: String, value: A, tags: Tag*): F[Unit] =
    state.update { oldState =>
      val updatedField = oldState.getOrElse(aspect, Vector.empty) :+ Record[Any](value, tags)
      oldState.updated(aspect, updatedField)
    }.void

  override def eventer: Eventer[F] = new Eventer[F] {
    override def send(event: Event, tags: Tag*): F[Unit] = updateState("", event, tags: _*)
  }

  override def withTags(tags: Tag*): EventFactory[F] = this

  override def withScope(name: String): EventFactory[F] = this
}
