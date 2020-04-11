package com.avast.datadog4s.api.event

//TODO add Date support
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
case class Event(
  title: String,
  text: String,
  date: Option[Long] = None,
  hostname: Option[String] = None,
  aggregationKey: Option[String] = None,
  priority: Priority = Priority.Normal,
  sourceTypeName: Option[String] = None,
  alertType: AlertType = AlertType.Info
)
