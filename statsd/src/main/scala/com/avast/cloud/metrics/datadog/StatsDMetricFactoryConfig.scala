package com.avast.cloud.metrics.datadog

import java.net.InetSocketAddress

import com.avast.cloud.metrics.datadog.api.Tag

case class StatsDMetricFactoryConfig(
  basePrefix: String,
  statsDServer: InetSocketAddress,
  defaultTags: scala.collection.immutable.Seq[Tag],
  sampleRate: Double = 1.0,
  queueSize: Int = 10000,
  enableExceptionTagging: Boolean
)
