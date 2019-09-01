package com.avast.datadog4s.extension.http4s

import com.avast.datadog4s.api.tag.TagValue
import org.http4s.Method
import org.http4s.metrics.TerminationType

object TagValues {
  implicit val methodTagValue: TagValue[Method] = TagValue[String].contramap(_.name)
  implicit val terminationTypeTagValue: TagValue[TerminationType] = TagValue[String].contramap {
    case TerminationType.Abnormal => "abnormal"
    case TerminationType.Error    => "error"
    case TerminationType.Timeout  => "timeout"
  }

}
