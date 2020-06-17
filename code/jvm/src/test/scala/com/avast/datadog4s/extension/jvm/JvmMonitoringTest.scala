package com.avast.datadog4s.extension.jvm

import java.time.Duration

import cats.effect.{ ContextShift, IO, Timer }
import com.avast.cloud.datadog4s.inmemory.MockMetricsFactory
import com.avast.datadog4s.extension.jvm.JvmMonitoring.Config
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import cats.syntax.flatMap._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class JvmMonitoringTest extends AnyFlatSpec with Matchers {
  private val ec: ExecutionContext            = scala.concurrent.ExecutionContext.Implicits.global
  implicit val contextShift: ContextShift[IO] = cats.effect.IO.contextShift(ec)
  implicit val timer: Timer[IO]               = IO.timer(ec)

  val noopErrHandler: Throwable => IO[Unit] = (_: Throwable) => IO.unit

  "JvmMonitoring" should "create all expected metrics and update them periodically" in {
    val testEffect = MockMetricsFactory.make[IO].flatMap { inmemory =>
      val runTest = JvmMonitoring
        .configured(inmemory, Config().copy(delay = Duration.ofMillis(10)), noopErrHandler)
        .use(_ => IO.never)
        .timeout(100.millis)
        .attempt

      runTest >> inmemory.state.get
    }
    val result     = testEffect.unsafeRunSync()
    result.keySet must equal(expectedAspects)
    result.values.foreach { vector =>
      vector.groupBy(_.tags).foreach {
        case (_, records) =>
          records.size must be > 0
          records.size must be < 15
      }
    }
  }

  val expectedAspects: Set[String] = Set(
    "jvm.cpu.load",
    "jvm.cpu.time",
    "jvm.filedescriptor.open",
    "jvm.heap_memory",
    "jvm.heap_memory_committed",
    "jvm.heap_memory_init",
    "jvm.heap_memory_max",
    "jvm.non_heap_memory",
    "jvm.non_heap_memory_committed",
    "jvm.non_heap_memory_init",
    "jvm.non_heap_memory_max",
    "jvm.uptime",
    "jvm.thread_count",
    "jvm.thread_daemon",
    "jvm.thread_started",
    "jvm.classes.loaded",
    "jvm.bufferpool.instances",
    "jvm.bufferpool.bytes",
    // we do not explicitly verify minor collection count/time as we don't know what GC is going to be used
    "jvm.gc.major_collection_time",
    "jvm.gc.major_collection_count"
  )

}
