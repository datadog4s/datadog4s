package com.avast.datadog4s.statsd.metric

import cats.effect.Sync
import com.avast.datadog4s.api.Tag
import com.avast.datadog4s.api.event._
import com.avast.datadog4s.api.metric.Eventer
import com.timgroup.statsd.{ StatsDClient, Event => SEvent }

import scala.collection.immutable.Seq
class EventerImpl[F[_]: Sync](statsDClient: StatsDClient, defaultTags: Seq[Tag]) extends Eventer[F] {
  private[this] val F = Sync[F]

  override def send(
    event: Event,
    tags: Tag*
  ): F[Unit] = {

    val eventBuilder: SEvent.Builder = SEvent.builder()
    eventBuilder.withTitle(event.title)
    eventBuilder.withText(event.text)

    event.date.foreach(eventBuilder.withDate)
    event.hostname.foreach(eventBuilder.withHostname)
    event.aggregationKey.foreach(eventBuilder.withAggregationKey)

    eventBuilder.withPriority(event.priority match {
      case Priority.Low    => SEvent.Priority.LOW
      case Priority.Normal => SEvent.Priority.NORMAL
    })

    event.sourceTypeName.foreach(eventBuilder.withSourceTypeName)

    eventBuilder.withAlertType(event.alertType match {
      case AlertType.Error   => SEvent.AlertType.ERROR
      case AlertType.Warning => SEvent.AlertType.WARNING
      case AlertType.Info    => SEvent.AlertType.INFO
      case AlertType.Success => SEvent.AlertType.SUCCESS
    })

    F.delay(statsDClient.recordEvent(eventBuilder.build(), (defaultTags ++ tags): _*))
  }

}
