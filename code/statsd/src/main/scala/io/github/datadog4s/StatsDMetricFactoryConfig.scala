package io.github.datadog4s

import io.github.datadog4s.api.Tag

import java.net.InetSocketAddress

case class StatsDMetricFactoryConfig(
    basePrefix: Option[String],
    statsDServer: InetSocketAddress,
    defaultTags: scala.collection.immutable.Seq[Tag] = Vector.empty,
    sampleRate: Double = 1.0,
    queueSize: Int = 10000
)
