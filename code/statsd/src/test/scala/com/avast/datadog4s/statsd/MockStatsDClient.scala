package com.avast.datadog4s.statsd

import java.util.concurrent.atomic.AtomicReference

import com.avast.datadog4s.statsd.MockStatsDClient.ExecutionTimeRecord
import com.timgroup.statsd.{Event, ServiceCheck, StatsDClient}

class MockStatsDClient(val history: AtomicReference[Vector[ExecutionTimeRecord]]) extends StatsDClient {
  override def stop(): Unit = ???

  override def close(): Unit = ???

  override def count(aspect: String, delta: Long, tags: String*): Unit = ???

  override def count(aspect: String, delta: Long, sampleRate: Double, tags: String*): Unit = ???

  override def count(aspect: String, delta: Double, tags: String*): Unit = ???

  override def count(aspect: String, delta: Double, sampleRate: Double, tags: String*): Unit = ???

  override def incrementCounter(aspect: String, tags: String*): Unit = ???

  override def incrementCounter(aspect: String, sampleRate: Double, tags: String*): Unit = ???

  override def increment(aspect: String, tags: String*): Unit = ???

  override def increment(aspect: String, sampleRate: Double, tags: String*): Unit = ???

  override def decrementCounter(aspect: String, tags: String*): Unit = ???

  override def decrementCounter(aspect: String, sampleRate: Double, tags: String*): Unit = ???

  override def decrement(aspect: String, tags: String*): Unit = ???

  override def decrement(aspect: String, sampleRate: Double, tags: String*): Unit = ???

  override def recordGaugeValue(aspect: String, value: Double, tags: String*): Unit = ???

  override def recordGaugeValue(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = ???

  override def recordGaugeValue(aspect: String, value: Long, tags: String*): Unit = ???

  override def recordGaugeValue(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = ???

  override def gauge(aspect: String, value: Double, tags: String*): Unit = ???

  override def gauge(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = ???

  override def gauge(aspect: String, value: Long, tags: String*): Unit = ???

  override def gauge(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = ???

  override def recordExecutionTime(aspect: String, timeInMs: Long, tags: String*): Unit = ???

  override def recordExecutionTime(aspect: String, timeInMs: Long, sampleRate: Double, tags: String*): Unit = {
    history.updateAndGet(h => h.appended(ExecutionTimeRecord(aspect, timeInMs, sampleRate, tags.toVector)))
    ()
  }

  override def time(aspect: String, value: Long, tags: String*): Unit = ???

  override def time(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = ???

  override def recordHistogramValue(aspect: String, value: Double, tags: String*): Unit = ???

  override def recordHistogramValue(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = ???

  override def recordHistogramValue(aspect: String, value: Long, tags: String*): Unit = ???

  override def recordHistogramValue(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = ???

  override def histogram(aspect: String, value: Double, tags: String*): Unit = ???

  override def histogram(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = ???

  override def histogram(aspect: String, value: Long, tags: String*): Unit = ???

  override def histogram(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = ???

  override def recordDistributionValue(aspect: String, value: Double, tags: String*): Unit = ???

  override def recordDistributionValue(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = ???

  override def recordDistributionValue(aspect: String, value: Long, tags: String*): Unit = ???

  override def recordDistributionValue(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = ???

  override def distribution(aspect: String, value: Double, tags: String*): Unit = ???

  override def distribution(aspect: String, value: Double, sampleRate: Double, tags: String*): Unit = ???

  override def distribution(aspect: String, value: Long, tags: String*): Unit = ???

  override def distribution(aspect: String, value: Long, sampleRate: Double, tags: String*): Unit = ???

  override def recordEvent(event: Event, tags: String*): Unit = ???

  override def recordServiceCheckRun(sc: ServiceCheck): Unit = ???

  override def serviceCheck(sc: ServiceCheck): Unit = ???

  override def recordSetValue(aspect: String, value: String, tags: String*): Unit = ???
}

object MockStatsDClient {
  case class ExecutionTimeRecord(aspect: String, value: Long, sampleRate: Double, tags: Vector[String])

  def apply(): MockStatsDClient = new MockStatsDClient(new AtomicReference[Vector[ExecutionTimeRecord]](Vector.empty))
}
