package io.github.datadog4s.extension.http4s

package object impl {
  private[http4s] type ActiveConnections = Map[Option[String], Int]
}
