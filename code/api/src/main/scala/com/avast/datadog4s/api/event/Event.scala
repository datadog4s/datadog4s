package com.avast.datadog4s.api.event

import java.time.Instant

import com.avast.datadog4s.api.event

/**
 *
 * @param title required - The title of the event
 * @param text required - The text body of the event
 * @param date required (defaults to the current time from the DogStatsD server) - The epoch timestamp for the event
 * @param hostname The name of the host
 * @param aggregationKey A key to use for aggregating events
 * @param priority Specifies the priority of the event (normal or low).
 * @param sourceTypeName The source type name
 * @param alertType error, warning, success, or info (defaults to info)
 */
sealed trait Event {
  val title: String
  val text: String
  val date: Option[Instant]          = None
  val hostname: Option[String]       = None
  val aggregationKey: Option[String] = None
  val priority: event.Priority.Value = Priority.Normal
  val sourceTypeName: Option[String] = None
  val alertType: AlertType.Value     = AlertType.Info
}

object Event {

  case class Error(
    title: String,
    text: String,
    override val date: Option[Instant] = None,
    override val hostname: Option[String] = None,
    override val aggregationKey: Option[String] = None,
    override val priority: Priority.Value = Priority.Normal,
    override val sourceTypeName: Option[String] = None
  ) extends Event {
    override val alertType: AlertType.Value = AlertType.Error
  }

  case class Warning(
    title: String,
    text: String,
    override val date: Option[Instant] = None,
    override val hostname: Option[String] = None,
    override val aggregationKey: Option[String] = None,
    override val priority: Priority.Value = Priority.Normal,
    override val sourceTypeName: Option[String] = None
  ) extends Event {
    override val alertType: AlertType.Value = AlertType.Warning
  }

  case class Info(
    title: String,
    text: String,
    override val date: Option[Instant] = None,
    override val hostname: Option[String] = None,
    override val aggregationKey: Option[String] = None,
    override val priority: Priority.Value = Priority.Normal,
    override val sourceTypeName: Option[String] = None
  ) extends Event {
    override val alertType: AlertType.Value = AlertType.Info
  }

  case class Success(
    title: String,
    text: String,
    override val date: Option[Instant] = None,
    override val hostname: Option[String] = None,
    override val aggregationKey: Option[String] = None,
    override val priority: Priority.Value = Priority.Normal,
    override val sourceTypeName: Option[String] = None
  ) extends Event {
    override val alertType: AlertType.Value = AlertType.Success
  }

}
