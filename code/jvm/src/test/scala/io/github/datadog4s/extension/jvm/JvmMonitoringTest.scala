package io.github.datadog4s.extension.jvm

import cats.effect.IO
import JvmMonitoring.Config
import io.github.cloud.datadog4s.inmemory.MockMetricsFactory

import java.time.Duration
import scala.concurrent.duration.*

class JvmMonitoringTest extends munit.FunSuite {
  import cats.effect.unsafe.implicits.global

  val noopErrHandler: Throwable => IO[Unit] = (_: Throwable) => IO.unit

  test("JvmMonitoring should create all expected metrics and update them periodically") {
    val testEffect = MockMetricsFactory.make[IO].flatMap { inmemory =>
      val runTest = JvmMonitoring
        .configured(inmemory, Config().copy(delay = Duration.ofMillis(100)), noopErrHandler)
        .use(_ => IO.never)
        .timeout(1000.millis)
        .attempt

      runTest >> inmemory.state.get
    }
    val result          = testEffect.unsafeRunSync()
    val observedAspects = (result.keySet -- unreliableAspects).toList.sorted
    assertEquals(observedAspects, expectedAspects.toList.sorted)
    result.values.foreach { vector =>
      vector.groupBy(_.tags).foreach { case (_, records) =>
        assert(records.nonEmpty)
        assert(records.size < 15)
      }
    }
  }

  /** Not always present and should be ignored
    */
  lazy val unreliableAspects: Set[String] = Set(
    "jvm.non_heap_memory.code_cache",
    "jvm.non_heap_memory.code_cache_committed",
    "jvm.non_heap_memory.code_cache_max"
  )

  lazy val minorGcParams: Set[String] =
    if (System.getProperty("java.version").startsWith("1.8."))
      Set.empty
    else Set("jvm.gc.minor_collection_time", "jvm.gc.minor_collection_count")

  lazy val expectedAspects: Set[String] = Set(
    "jvm.cpu.load",
    "jvm.cpu.time",
    "jvm.filedescriptor.open",
    "jvm.heap_memory",
    "jvm.heap_memory_committed",
    "jvm.heap_memory_init",
    "jvm.heap_memory_max",
    "jvm.heap_memory.eden",
    "jvm.heap_memory.eden_committed",
    "jvm.heap_memory.eden_max",
    "jvm.heap_memory.survivor",
    "jvm.heap_memory.survivor_committed",
    "jvm.heap_memory.survivor_max",
    "jvm.heap_memory.old_gen",
    "jvm.heap_memory.old_gen_committed",
    "jvm.heap_memory.old_gen_max",
    "jvm.non_heap_memory",
    "jvm.non_heap_memory_committed",
    "jvm.non_heap_memory_init",
    "jvm.non_heap_memory_max",
    "jvm.non_heap_memory.metaspace",
    "jvm.non_heap_memory.metaspace_committed",
    "jvm.non_heap_memory.metaspace_max",
    "jvm.non_heap_memory.compressed_class_space",
    "jvm.non_heap_memory.compressed_class_space_committed",
    "jvm.non_heap_memory.compressed_class_space_max",
    "jvm.uptime",
    "jvm.thread_count",
    "jvm.thread_daemon",
    "jvm.thread_started",
    "jvm.loaded_classes",
    "jvm.bufferpool.instances",
    "jvm.bufferpool.bytes",
    "jvm.gc.major_collection_time",
    "jvm.gc.major_collection_count"
  ) ++ minorGcParams
}
