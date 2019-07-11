package com.avast.cloud.metrics.datadog

import java.net.InetSocketAddress

case class MetricFactoryConfig(prefix: String, statsDServer: InetSocketAddress, defaultTags: Map[String, String])
