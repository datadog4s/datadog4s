package com.avast.datadog4s.statsd

import java.net.InetSocketAddress

import cats.effect.{ Resource, Sync }
import com.timgroup.statsd.{ NonBlockingStatsDClient, NonBlockingStatsDClientBuilder }

object StatsDClient {
  private def makeBuilder(statsDServer: InetSocketAddress, queueSize: Int): NonBlockingStatsDClientBuilder = {
    new NonBlockingStatsDClientBuilder()
      .hostname(statsDServer.getHostName)
      .port(statsDServer.getPort)
      .queueSize(queueSize)
      .prefix("")
  }

  def makeUnsafe(statsDServer: InetSocketAddress, queueSize: Int): NonBlockingStatsDClient = {
    makeBuilder(statsDServer, queueSize).build()
  }

  def makeResource[F[_]: Sync](statsDServer: InetSocketAddress, queueSize: Int): Resource[F, NonBlockingStatsDClient] =
    Resource.fromAutoCloseable(Sync[F].delay(makeBuilder(statsDServer, queueSize).build()))
}
