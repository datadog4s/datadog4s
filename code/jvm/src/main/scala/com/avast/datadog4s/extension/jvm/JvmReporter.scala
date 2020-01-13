package com.avast.datadog4s.extension.jvm

import java.lang.management.ManagementFactory

import cats.Traverse
import cats.effect.Sync
import cats.instances.vector._
import cats.syntax.flatMap._
import com.avast.datadog4s.api.metric.Gauge
import com.avast.datadog4s.api.{ MetricFactory, Tag }
import com.sun.management._
import sun.management.ManagementFactoryHelper

import scala.jdk.CollectionConverters._

class JvmReporter[F[_]: Sync](metricsFactory: MetricFactory[F]) {
  private val F = Sync[F]

  private val cpuLoad              = metricsFactory.gauge.double("jvm.cpu.load")
  private val cpuTime              = metricsFactory.gauge.long("jvm.cpu.time")
  private val openFds              = metricsFactory.gauge.long("jvm.filedescriptor.open")
  private val heapUsed             = metricsFactory.gauge.long("jvm.heap.used")
  private val heapCommitted        = metricsFactory.gauge.long("jvm.heap.committed")
  private val heapMax              = metricsFactory.gauge.long("jvm.heap.max")
  private val nonHeapUsed          = metricsFactory.gauge.long("jvm.nonheap.used")
  private val nonHeapCommitted     = metricsFactory.gauge.long("jvm.nonheap.committed")
  private val uptime               = metricsFactory.gauge.long("jvm.uptime")
  private val threadsTotal         = metricsFactory.gauge.long("jvm.threads.total")
  private val threadsDaemon        = metricsFactory.gauge.long("jvm.threads.daemon")
  private val threadsStarted       = metricsFactory.gauge.long("jvm.threads.started")
  private val classes              = metricsFactory.gauge.long("jvm.classes.loaded")
  private val bufferPoolsInstances = metricsFactory.gauge.long("jvm.bufferpool.instances")
  private val bufferPoolsBytes     = metricsFactory.gauge.long("jvm.bufferpool.bytes")
  private val gcCollections        = metricsFactory.gauge.long("jvm.gc.collections")
  private val gcTime               = metricsFactory.gauge.long("jvm.gc.time")

  private val osBean      = F.delay { ManagementFactory.getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean] }
  private val unixBean    = F.delay { ManagementFactory.getOperatingSystemMXBean.asInstanceOf[UnixOperatingSystemMXBean] }
  private val memoryBean  = ManagementFactory.getMemoryMXBean
  private val runtimeBean = ManagementFactory.getRuntimeMXBean
  private val threadBean  = ManagementFactory.getThreadMXBean
  private val classBean   = ManagementFactory.getClassLoadingMXBean
  private val bufferBeans = ManagementFactoryHelper.getBufferPoolMXBeans.asScala.toVector
  private val gcBeans     = ManagementFactory.getGarbageCollectorMXBeans.asScala.toVector

  private def wrapUnsafe[T](gauge: Gauge[F, T], tags: Tag*)(f: => T): F[Unit] =
    F.delay(f).flatMap(gauge.set(_, tags: _*))

  private val gc: Vector[F[Unit]] =
    gcBeans.map { bean =>
      val gcName = Tag.of("gc_name", bean.getName.replace(" ", "_"))
      wrapUnsafe(gcCollections, gcName)(bean.getCollectionCount) >>
        wrapUnsafe(gcTime, gcName)(bean.getCollectionTime)
    }

  private val buffers: Vector[F[Unit]] = bufferBeans.map { bean =>
    val beanName = Tag.of("buffer_pool", bean.getName)
    wrapUnsafe(bufferPoolsBytes, beanName)(bean.getMemoryUsed) >>
      wrapUnsafe(bufferPoolsInstances, beanName)(bean.getCount)
  }

  protected[jvm] val getBuffersIO          = Traverse[Vector].sequence(buffers)
  protected[jvm] val getGcIO               = Traverse[Vector].sequence(gc)
  protected[jvm] val getCpuLoadIO          = protect(osBean)(bean => wrapUnsafe(cpuLoad)(bean.getProcessCpuLoad))
  protected[jvm] val getCpuTimeIO          = protect(osBean)(bean => wrapUnsafe(cpuTime)(bean.getProcessCpuTime))
  protected[jvm] val getOpenFDsCountIO     = protect(unixBean)(bean => wrapUnsafe(openFds)(bean.getOpenFileDescriptorCount))
  protected[jvm] val getHeapUsedIO         = wrapUnsafe(heapUsed)(memoryBean.getHeapMemoryUsage.getUsed)
  protected[jvm] val getHeapCommittedIO    = wrapUnsafe(heapCommitted)(memoryBean.getHeapMemoryUsage.getCommitted)
  protected[jvm] val getHeapMaxIO          = wrapUnsafe(heapMax)(memoryBean.getHeapMemoryUsage.getMax)
  protected[jvm] val getNonHeapCommittedIO = wrapUnsafe(nonHeapCommitted)(memoryBean.getNonHeapMemoryUsage.getCommitted)
  protected[jvm] val getNonHeapUsedIO      = wrapUnsafe(nonHeapUsed)(memoryBean.getNonHeapMemoryUsage.getUsed)
  protected[jvm] val getUptimeIO           = wrapUnsafe(uptime)(runtimeBean.getUptime)
  protected[jvm] val getThreadsTotalIO     = wrapUnsafe(threadsTotal)(threadBean.getThreadCount.toLong)
  protected[jvm] val getThreadsDaemonIO    = wrapUnsafe(threadsDaemon)(threadBean.getDaemonThreadCount.toLong)
  protected[jvm] val getThreadsStartedIO   = wrapUnsafe(threadsStarted)(threadBean.getTotalStartedThreadCount)
  protected[jvm] val getClassesIO          = wrapUnsafe(classes)(classBean.getLoadedClassCount.toLong)

  private def protect[A](fa: F[A])(fu: A => F[Unit]): F[Unit] =
    F.recoverWith(fa.flatMap(fu)) {
      case _ => F.unit
    }

  val collect: F[Unit] =
    getBuffersIO >>
      getGcIO >>
      getCpuLoadIO >>
      getCpuTimeIO >>
      getOpenFDsCountIO >>
      getHeapUsedIO >>
      getHeapCommittedIO >>
      getHeapMaxIO >>
      getNonHeapCommittedIO >>
      getNonHeapUsedIO >>
      getUptimeIO >>
      getThreadsTotalIO >>
      getThreadsDaemonIO >>
      getThreadsStartedIO >>
      getClassesIO
}
