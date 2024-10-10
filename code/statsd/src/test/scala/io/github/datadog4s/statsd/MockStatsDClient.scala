package io.github.datadog4s.statsd

import java.util
import java.util.concurrent.atomic.AtomicReference

object MockStatsDClient {
  def apply(): JMockStatsDClient = new JMockStatsDClient(new AtomicReference(new util.ArrayList()))
}
