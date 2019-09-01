package com.avast.datadog4s.api.tag

trait TagValue[A] { self =>
  def convert(a: A): String

  def contramap[B](f: B => A): TagValue[B] = (a: B) => self.convert(f(a))
}

object TagValue {
  def apply[A: TagValue]: TagValue[A] = implicitly[TagValue[A]]

  implicit val stringTagValue: TagValue[String]            = identity[String]
  implicit val booleanTagValue: TagValue[Boolean]          = TagValue[String].contramap[Boolean](_.toString)
  implicit val classTagValue: TagValue[java.lang.Class[_]] = TagValue[String].contramap(_.getName)
  implicit val exceptionTagValue: TagValue[Exception]      = TagValue[Class[_]].contramap(_.getClass)
  implicit val throwableTagValue: TagValue[Throwable]      = TagValue[Class[_]].contramap(_.getClass)
}
