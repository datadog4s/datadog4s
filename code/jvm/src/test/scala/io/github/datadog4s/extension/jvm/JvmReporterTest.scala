package io.github.datadog4s.extension.jvm

import cats.effect.IO
import io.github.datadog4s.noop.NoopMetricFactory

class JvmReporterTest extends munit.FunSuite {
  val reporter = new JvmReporter[IO](new NoopMetricFactory[IO])

  test("JvmReporter should not throw any exceptions") {
    reporter.collect.unsafeRunSync()
  }

  test("JvmReporter should not throw exception when accessing individual metrics") {
    reporter.getBuffersIO.void.unsafeRunSync()
    reporter.getGcIO.void.unsafeRunSync()
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
    reporter.getThreadsStartedIO.void.unsafeRunSync()
    reporter.getClassesIO.void.unsafeRunSync()
  }

}
