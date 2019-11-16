package com.avast.datadog4s.extension.jvm

import cats.effect.IO
import com.avast.datadog4s.noop.NoopMetricFactory
import org.scalatest.FlatSpec

class JvmReporterTest extends FlatSpec {
  val reporter = new JvmReporter[IO](new NoopMetricFactory[IO])

  "JvmReporter" should "not throw any exceptions" in {
    reporter.collect.unsafeRunSync()
  }

  it should "not throw exception when accessing individual metrics" in {
    reporter.getBuffersIO.unsafeRunSync()
    reporter.getGcIO.unsafeRunSync()
    reporter.getCpuLoadIO.unsafeRunSync()
    reporter.getCpuTimeIO.unsafeRunSync()
    reporter.getOpenFDsCountIO.unsafeRunSync()
    reporter.getHeapUsedIO.unsafeRunSync()
    reporter.getHeapCommittedIO.unsafeRunSync()
    reporter.getHeapMaxIO.unsafeRunSync()
    reporter.getNonHeapCommittedIO.unsafeRunSync()
    reporter.getNonHeapUsedIO.unsafeRunSync()
    reporter.getUptimeIO.unsafeRunSync()
    reporter.getThreadsTotalIO.unsafeRunSync()
    reporter.getThreadsDaemonIO.unsafeRunSync()
    reporter.getThreadsStartedIO.unsafeRunSync()
    reporter.getClassesIO.unsafeRunSync()
  }

}
