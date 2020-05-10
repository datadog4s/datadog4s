package com.avast.datadog4s.statsd.event

import cats.effect.Sync
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.event.{ Eventer, _ }
import com.timgroup.statsd.{ StatsDClient, Event => SEvent }

import scala.collection.immutable.Seq
class EventerImpl[F[_]: Sync](statsDClient: StatsDClient, defaultTags: Seq[Tag]) extends Eventer[F] {
  private[this] val F = Sync[F]

  private val toPriority: Priority.Value => SEvent.Priority = {
    case Priority.Low    => SEvent.Priority.LOW
    case Priority.Normal => SEvent.Priority.NORMAL
  }

  private val toAlertType: AlertType.Value => SEvent.AlertType = {
    case AlertType.Error   => SEvent.AlertType.ERROR
    case AlertType.Warning => SEvent.AlertType.WARNING
    case AlertType.Info    => SEvent.AlertType.INFO
    case AlertType.Success => SEvent.AlertType.SUCCESS
  }

  override def send(
    event: Event,
    tags: Tag*
  ): F[Unit] =
    F.delay {
      statsDClient.recordEvent(
        SEvent
          .builder()
          .withTitle(event.title)
          .withText(event.text)
          .withDate(event.date.map(_.toEpochMilli).getOrElse[Long](-1))
          .withHostname(event.hostname.orNull)
          .withAggregationKey(event.aggregationKey.orNull)
          .withPriority(toPriority(event.priority))
          .withSourceTypeName(event.sourceTypeName.orNull)
          .withAlertType(toAlertType(event.alertType))
          .build(),
        (defaultTags ++ tags): _*
      )
    }

}
