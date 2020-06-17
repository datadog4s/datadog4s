package com.avast.datadog4s.statsd

import java.net.InetSocketAddress

import cats.effect.{ Resource, Sync }
import com.timgroup.statsd.{ NonBlockingStatsDClient, NonBlockingStatsDClientBuilder }

object StatsDClient {
  def make[F[_]: Sync](statsDServer: InetSocketAddress, queueSize: Int): Resource[F, NonBlockingStatsDClient] = {
    val builder = new NonBlockingStatsDClientBuilder()
      .hostname(statsDServer.getHostName)
      .port(statsDServer.getPort)
      .queueSize(queueSize)
      .prefix("")
    fromBuilder(builder)
  }

  def fromBuilder[F[_]: Sync](builder: NonBlockingStatsDClientBuilder): Resource[F, NonBlockingStatsDClient] =
    Resource.fromAutoCloseable(Sync[F].delay(builder.build()))
}
