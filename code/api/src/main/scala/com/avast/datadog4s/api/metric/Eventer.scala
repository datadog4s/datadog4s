package com.avast.datadog4s.api.metric

import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.event.{ AlertType, Event }

trait Eventer[F[_]] {

  def send(
    event: Event,
    tags: Tag*
  ): F[Unit]

  def error(
    event: Event,
    tags: Tag*
  ): F[Unit] =
    send(event.copy(alertType = AlertType.Error), tags: _*)

  def warning(
    event: Event,
    tags: Tag*
  ): F[Unit] =
    send(event.copy(alertType = AlertType.Warning), tags: _*)

  def info(
    event: Event,
    tags: Tag*
  ): F[Unit] =
    send(event.copy(alertType = AlertType.Info), tags: _*)

  def success(
    event: Event,
    tags: Tag*
  ): F[Unit] =
    send(event.copy(alertType = AlertType.Success), tags: _*)

}
