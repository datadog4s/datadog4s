package io.github.datadog4s.api.tag

@FunctionalInterface
trait TagValue[A] { self =>
  def convert(a: A): String

  def contramap[B](f: B => A): TagValue[B] = (a: B) => self.convert(f(a))
}

object TagValue {
  def apply[A: TagValue]: TagValue[A] = implicitly[TagValue[A]]

  implicit val stringTagValue: TagValue[String]            = identity[String]
  implicit val intTagValue: TagValue[Int]                  = TagValue[String].contramap(_.toString)
  implicit val longTagValue: TagValue[Long]                = TagValue[String].contramap(_.toString)
  implicit val shortTagValue: TagValue[Short]              = TagValue[String].contramap(_.toString)
  implicit val doubleTagValue: TagValue[Double]            = TagValue[String].contramap(_.toString)
  implicit val floatTagValue: TagValue[Float]              = TagValue[String].contramap(_.toString)
  implicit val booleanTagValue: TagValue[Boolean]          = TagValue[String].contramap(_.toString)
  implicit val classTagValue: TagValue[java.lang.Class[?]] = TagValue[String].contramap(_.getName)
  implicit val exceptionTagValue: TagValue[Exception]      = TagValue[Class[?]].contramap(_.getClass)
  implicit val throwableTagValue: TagValue[Throwable]      = TagValue[Class[?]].contramap(_.getClass)
}
