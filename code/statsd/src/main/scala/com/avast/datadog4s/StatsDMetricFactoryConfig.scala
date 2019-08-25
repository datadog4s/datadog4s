package com.avast.datadog4s

import java.net.InetSocketAddress

import com.avast.datadog4s.api.Tag

case class StatsDMetricFactoryConfig(
  basePrefix: String,
  statsDServer: InetSocketAddress,
  defaultTags: scala.collection.immutable.Seq[Tag] = Vector.empty,
  sampleRate: Double = 1.0,
  queueSize: Int = 10000
)
