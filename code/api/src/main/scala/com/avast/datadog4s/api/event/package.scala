package com.avast.datadog4s.api

import enumeratum.{ Enum, EnumEntry }

package object event {
  sealed trait Priority extends EnumEntry

  object Priority extends Enum[Priority] {

    val values = findValues

    case object Low    extends Priority
    case object Normal extends Priority
  }

  sealed trait AlertType extends EnumEntry

  object AlertType extends Enum[AlertType] {

    val values = findValues

    case object Error   extends AlertType
    case object Warning extends AlertType
    case object Info    extends AlertType
    case object Success extends AlertType
  }
}
