package com.avast.datadog4s.api.tag

trait TagValue[A] { self =>
  def convert(a: A): String

  def contramap[B](f: B => A): TagValue[B] = (a: B) => self.convert(f(a))
}

object TagValue {
  def apply[A: TagValue]: TagValue[A] = implicitly[TagValue[A]]

  implicit val string: TagValue[String]            = identity[String]
  implicit val boolean: TagValue[Boolean]          = TagValue[String].contramap[Boolean](_.toString)
  implicit val clazz: TagValue[java.lang.Class[_]] = TagValue[String].contramap(_.getName)
}
