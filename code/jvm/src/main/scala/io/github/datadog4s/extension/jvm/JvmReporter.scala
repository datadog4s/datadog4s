package io.github.datadog4s.extension.jvm

import java.lang.management.ManagementFactory

import cats.Traverse
import cats.effect.Sync
import cats.syntax.flatMap.*
import io.github.datadog4s.api.{MetricFactory, Tag}
import com.sun.management.*
import io.github.datadog4s.api.metric.Gauge
import sun.management.ManagementFactoryHelper

import scala.jdk.CollectionConverters.*

class JvmReporter[F[_]: Sync](metricsFactory: MetricFactory[F]) {
  private val F = Sync[F]

  private val cpuLoad                         = metricsFactory.gauge.double("jvm.cpu.load")
  private val cpuTime                         = metricsFactory.gauge.long("jvm.cpu.time")
  private val openFds                         = metricsFactory.gauge.long("jvm.filedescriptor.open")
  private val heapUsed                        = metricsFactory.gauge.long("jvm.heap_memory")
  private val heapCommitted                   = metricsFactory.gauge.long("jvm.heap_memory_committed")
  private val heapInit                        = metricsFactory.gauge.long("jvm.heap_memory_init")
  private val heapMax                         = metricsFactory.gauge.long("jvm.heap_memory_max")
  private val heapEdenUsed                    = metricsFactory.gauge.long("jvm.heap_memory.eden")
  private val heapEdenCommitted               = metricsFactory.gauge.long("jvm.heap_memory.eden_committed")
  private val heapEdenMax                     = metricsFactory.gauge.long("jvm.heap_memory.eden_max")
  private val heapSurvivorUsed                = metricsFactory.gauge.long("jvm.heap_memory.survivor")
  private val heapSurvivorCommitted           = metricsFactory.gauge.long("jvm.heap_memory.survivor_committed")
  private val heapSurvivorMax                 = metricsFactory.gauge.long("jvm.heap_memory.survivor_max")
  private val heapOldGenUsed                  = metricsFactory.gauge.long("jvm.heap_memory.old_gen")
  private val heapOldGenCommitted             = metricsFactory.gauge.long("jvm.heap_memory.old_gen_committed")
  private val heapOldGenMax                   = metricsFactory.gauge.long("jvm.heap_memory.old_gen_max")
  private val nonHeapUsed                     = metricsFactory.gauge.long("jvm.non_heap_memory")
  private val nonHeapCommitted                = metricsFactory.gauge.long("jvm.non_heap_memory_committed")
  private val nonHeapInit                     = metricsFactory.gauge.long("jvm.non_heap_memory_init")
  private val nonHeapMax                      = metricsFactory.gauge.long("jvm.non_heap_memory_max")
  private val nonHeapCodeCacheUsed            = metricsFactory.gauge.long("jvm.non_heap_memory.code_cache")
  private val nonHeapCodeCacheCommitted       = metricsFactory.gauge.long("jvm.non_heap_memory.code_cache_committed")
  private val nonHeapCodeCacheMax             = metricsFactory.gauge.long("jvm.non_heap_memory.code_cache_max")
  private val nonHeapMetaspaceUsed            = metricsFactory.gauge.long("jvm.non_heap_memory.metaspace")
  private val nonHeapMetaspaceCommitted       = metricsFactory.gauge.long("jvm.non_heap_memory.metaspace_committed")
  private val nonHeapMetaspaceMax             = metricsFactory.gauge.long("jvm.non_heap_memory.metaspace_max")
  private val nonHeapCompressedClassSpaceUsed = metricsFactory.gauge.long("jvm.non_heap_memory.compressed_class_space")
  private val nonHeapCompressedClassSpaceCommitted =
    metricsFactory.gauge.long("jvm.non_heap_memory.compressed_class_space_committed")
  private val nonHeapCompressedClassSpaceMax =
    metricsFactory.gauge.long("jvm.non_heap_memory.compressed_class_space_max")
  private val uptime               = metricsFactory.gauge.long("jvm.uptime")
  private val threadsTotal         = metricsFactory.gauge.long("jvm.thread_count")
  private val threadsDaemon        = metricsFactory.gauge.long("jvm.thread_daemon")
  private val threadsStarted       = metricsFactory.gauge.long("jvm.thread_started")
  private val classes              = metricsFactory.gauge.long("jvm.loaded_classes")
  private val bufferPoolsInstances = metricsFactory.gauge.long("jvm.bufferpool.instances")
  private val bufferPoolsBytes     = metricsFactory.gauge.long("jvm.bufferpool.bytes")
  private val gcMinorCollections   = metricsFactory.gauge.long("jvm.gc.minor_collection_count")
  private val gcMinorTime          = metricsFactory.gauge.long("jvm.gc.minor_collection_time")
  private val gcMajorCollections   = metricsFactory.gauge.long("jvm.gc.major_collection_count")
  private val gcMajorTime          = metricsFactory.gauge.long("jvm.gc.major_collection_time")

  private val osBean      = F.delay(ManagementFactory.getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean])
  private val unixBean    = F.delay(ManagementFactory.getOperatingSystemMXBean.asInstanceOf[UnixOperatingSystemMXBean])
  private val memoryBean  = ManagementFactory.getMemoryMXBean
  private val runtimeBean = ManagementFactory.getRuntimeMXBean
  private val threadBean  = ManagementFactory.getThreadMXBean
  private val classBean   = ManagementFactory.getClassLoadingMXBean
  private val bufferBeans = ManagementFactoryHelper.getBufferPoolMXBeans.asScala.toVector
  private val gcBeans     = ManagementFactory.getGarbageCollectorMXBeans.asScala.toVector

  private def wrapUnsafe[T](gauge: Gauge[F, T], tags: Tag*)(f: => T): F[Unit] =
    F.delay(f).flatMap(gauge.set(_, tags*))

  private val gc: Vector[F[Unit]] =
    gcBeans.map { bean =>
      val name   = bean.getName
      val gcName = Tag.of("gc_name", name.replace(" ", "_"))
      if (name.toLowerCase.contains("young"))
        wrapUnsafe(gcMinorCollections, gcName)(bean.getCollectionCount) >>
          wrapUnsafe(gcMinorTime, gcName)(bean.getCollectionTime)
      else
        wrapUnsafe(gcMajorCollections, gcName)(bean.getCollectionCount) >>
          wrapUnsafe(gcMajorTime, gcName)(bean.getCollectionTime)
    }

  private val buffers: Vector[F[Unit]] = bufferBeans.map { bean =>
    val beanName = Tag.of("buffer_pool", bean.getName)
    wrapUnsafe(bufferPoolsBytes, beanName)(bean.getMemoryUsed) >>
      wrapUnsafe(bufferPoolsInstances, beanName)(bean.getCount)
  }

  private val getEdenIO = {
    ManagementFactory.getMemoryPoolMXBeans.asScala.find(_.getName.endsWith("Eden Space")) match {
      case Some(bean) =>
        wrapUnsafe(heapEdenUsed)(bean.getUsage.getUsed) >>
          wrapUnsafe(heapEdenCommitted)(bean.getUsage.getCommitted) >>
          wrapUnsafe(heapEdenMax)(bean.getUsage.getMax)
      case None =>
        F.unit
    }
  }

  private val getSurvivorIO = {
    ManagementFactory.getMemoryPoolMXBeans.asScala.find(_.getName.endsWith("Survivor Space")) match {
      case Some(bean) =>
        wrapUnsafe(heapSurvivorUsed)(bean.getUsage.getUsed) >>
          wrapUnsafe(heapSurvivorCommitted)(bean.getUsage.getCommitted) >>
          wrapUnsafe(heapSurvivorMax)(bean.getUsage.getMax)
      case None =>
        F.unit
    }
  }

  private val getOldGenIO = {
    ManagementFactory.getMemoryPoolMXBeans.asScala.find(_.getName.endsWith("Old Gen")) match {
      case Some(bean) =>
        wrapUnsafe(heapOldGenUsed)(bean.getUsage.getUsed) >>
          wrapUnsafe(heapOldGenCommitted)(bean.getUsage.getCommitted) >>
          wrapUnsafe(heapOldGenMax)(bean.getUsage.getMax)
      case None =>
        F.unit
    }
  }

  private val getCodeCacheIO = {
    ManagementFactory.getMemoryPoolMXBeans.asScala.find(bean =>
      bean.getName == "CodeCache" || bean.getName == "Code Cache"
    ) match {
      case Some(bean) =>
        wrapUnsafe(nonHeapCodeCacheUsed)(bean.getUsage.getUsed) >>
          wrapUnsafe(nonHeapCodeCacheCommitted)(bean.getUsage.getCommitted) >>
          wrapUnsafe(nonHeapCodeCacheMax)(bean.getUsage.getMax)
      case None =>
        F.unit
    }
  }

  private val getMetaspaceIO = {
    ManagementFactory.getMemoryPoolMXBeans.asScala.find(_.getName == "Metaspace") match {
      case Some(bean) =>
        wrapUnsafe(nonHeapMetaspaceUsed)(bean.getUsage.getUsed) >>
          wrapUnsafe(nonHeapMetaspaceCommitted)(bean.getUsage.getCommitted) >>
          wrapUnsafe(nonHeapMetaspaceMax)(bean.getUsage.getMax)
      case None =>
        F.unit
    }
  }

  private val getCompressedClassSpaceIO = {
    ManagementFactory.getMemoryPoolMXBeans.asScala.find(_.getName == "Compressed Class Space") match {
      case Some(bean) =>
        wrapUnsafe(nonHeapCompressedClassSpaceUsed)(bean.getUsage.getUsed) >>
          wrapUnsafe(nonHeapCompressedClassSpaceCommitted)(bean.getUsage.getCommitted) >>
          wrapUnsafe(nonHeapCompressedClassSpaceMax)(bean.getUsage.getMax)
      case None =>
        F.unit
    }
  }

  protected[jvm] val getBuffersIO: F[Vector[Unit]] = Traverse[Vector].sequence(buffers)
  protected[jvm] val getGcIO: F[Vector[Unit]]      = Traverse[Vector].sequence(gc)
  protected[jvm] val getCpuLoadIO: F[Unit] = protect(osBean)(bean => wrapUnsafe(cpuLoad)(bean.getProcessCpuLoad))
  protected[jvm] val getCpuTimeIO: F[Unit] = protect(osBean)(bean => wrapUnsafe(cpuTime)(bean.getProcessCpuTime))
  protected[jvm] val getOpenFDsCountIO: F[Unit] =
    protect(unixBean)(bean => wrapUnsafe(openFds)(bean.getOpenFileDescriptorCount))
  protected[jvm] val getHeapUsedIO: F[Unit]      = wrapUnsafe(heapUsed)(memoryBean.getHeapMemoryUsage.getUsed)
  protected[jvm] val getHeapCommittedIO: F[Unit] = wrapUnsafe(heapCommitted)(memoryBean.getHeapMemoryUsage.getCommitted)
  protected[jvm] val getHeapInitIO: F[Unit]      = wrapUnsafe(heapInit)(memoryBean.getHeapMemoryUsage.getInit)
  protected[jvm] val getHeapMaxIO: F[Unit]       = wrapUnsafe(heapMax)(memoryBean.getHeapMemoryUsage.getMax)
  protected[jvm] val getNonHeapCommittedIO: F[Unit] =
    wrapUnsafe(nonHeapCommitted)(memoryBean.getNonHeapMemoryUsage.getCommitted)
  protected[jvm] val getNonHeapUsedIO: F[Unit]    = wrapUnsafe(nonHeapUsed)(memoryBean.getNonHeapMemoryUsage.getUsed)
  protected[jvm] val getNonHeapInitIO: F[Unit]    = wrapUnsafe(nonHeapInit)(memoryBean.getNonHeapMemoryUsage.getInit)
  protected[jvm] val getNonHeapMaxIO: F[Unit]     = wrapUnsafe(nonHeapMax)(memoryBean.getNonHeapMemoryUsage.getMax)
  protected[jvm] val getUptimeIO: F[Unit]         = wrapUnsafe(uptime)(runtimeBean.getUptime)
  protected[jvm] val getThreadsTotalIO: F[Unit]   = wrapUnsafe(threadsTotal)(threadBean.getThreadCount.toLong)
  protected[jvm] val getThreadsDaemonIO: F[Unit]  = wrapUnsafe(threadsDaemon)(threadBean.getDaemonThreadCount.toLong)
  protected[jvm] val getThreadsStartedIO: F[Unit] = wrapUnsafe(threadsStarted)(threadBean.getTotalStartedThreadCount)
  protected[jvm] val getClassesIO: F[Unit]        = wrapUnsafe(classes)(classBean.getLoadedClassCount.toLong)

  private def protect[A](fa: F[A])(fu: A => F[Unit]): F[Unit] =
    F.recoverWith(fa.flatMap(fu)) { case _ =>
      F.unit
    }

  val collect: F[Unit] =
    getBuffersIO >>
      getGcIO >>
      getCpuLoadIO >>
      getCpuTimeIO >>
      getOpenFDsCountIO >>
      getHeapUsedIO >>
      getHeapCommittedIO >>
      getHeapInitIO >>
      getHeapMaxIO >>
      getEdenIO >>
      getSurvivorIO >>
      getOldGenIO >>
      getNonHeapCommittedIO >>
      getNonHeapUsedIO >>
      getNonHeapInitIO >>
      getNonHeapMaxIO >>
      getCodeCacheIO >>
      getMetaspaceIO >>
      getCompressedClassSpaceIO >>
      getUptimeIO >>
      getThreadsTotalIO >>
      getThreadsDaemonIO >>
      getThreadsStartedIO >>
      getClassesIO
}
