package com.avast.datadog4s.extension.jvm

import java.lang.management.ManagementFactory

import cats.Traverse
import cats.effect.Sync
import cats.instances.vector._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.avast.datadog4s.api.{ MetricFactory, Tag }
import com.sun.management._
import sun.management.ManagementFactoryHelper

import scala.collection.JavaConverters._

class JvmReporter[F[_]: Sync](metricsFactory: MetricFactory[F]) {
  private val F = Sync[F]

  private val cpuLoad              = metricsFactory.gauge.double("jvm.cpu.load")
  private val cpuTime              = metricsFactory.gauge.long("jvm.cpu.time")
  private val openFds              = metricsFactory.gauge.long("jvm.filedescriptor.open")
  private val heapUsed             = metricsFactory.gauge.long("jvm.heap.used")
  private val heapCommitted        = metricsFactory.gauge.long("jvm.heap.committed")
  private val heapMax              = metricsFactory.gauge.long("jvm.heap.max")
  private val nonHeapUsed          = metricsFactory.gauge.long("jvm.nonheap.used")
  private val nonHeapCommited      = metricsFactory.gauge.long("jvm.nonheap.commited")
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

  def collect: F[Unit] =
    Traverse[Vector].sequence(buffers) >>
      Traverse[Vector].sequence(gc) >>
      getBuffersIO >>
      getGcIO >>
      getCpuLoadIO >>
      getCpuTimeIO >>
      getOpenFDsCountIO >>
      getHeapUsedIO >>
      getHeapCommittedIO >>
      getHeapMaxIO >>
      getNonHeapCommitedIO >>
      getNonHeapUsedIO >>
      getUptimeIO >>
      getThreadsTotalIO >>
      getThreadsDaemonIO >>
      getThreadsStartedIO >>
      getClassesIO

  protected[jvm] val getBuffersIO         = Traverse[Vector].sequence(buffers)
  protected[jvm] val getGcIO              = Traverse[Vector].sequence(gc)
  protected[jvm] val getCpuLoadIO         = protect(osBean)(bean => cpuLoad.set(bean.getProcessCpuLoad))
  protected[jvm] val getCpuTimeIO         = protect(osBean)(bean => cpuLoad.set(bean.getProcessCpuLoad))
  protected[jvm] val getOpenFDsCountIO    = protect(unixBean)(bean => openFds.set(bean.getOpenFileDescriptorCount))
  protected[jvm] val getHeapUsedIO        = heapUsed.set(memoryBean.getHeapMemoryUsage.getUsed)
  protected[jvm] val getHeapCommittedIO   = heapCommitted.set(memoryBean.getHeapMemoryUsage.getCommitted)
  protected[jvm] val getHeapMaxIO         = heapMax.set(memoryBean.getHeapMemoryUsage.getMax)
  protected[jvm] val getNonHeapCommitedIO = nonHeapCommited.set(memoryBean.getNonHeapMemoryUsage.getCommitted)
  protected[jvm] val getNonHeapUsedIO     = nonHeapUsed.set(memoryBean.getNonHeapMemoryUsage.getUsed)
  protected[jvm] val getUptimeIO          = uptime.set(runtimeBean.getUptime)
  protected[jvm] val getThreadsTotalIO    = threadsTotal.set(threadBean.getThreadCount.toLong)
  protected[jvm] val getThreadsDaemonIO   = threadsDaemon.set(threadBean.getDaemonThreadCount.toLong)
  protected[jvm] val getThreadsStartedIO  = threadsStarted.set(threadBean.getTotalStartedThreadCount)
  protected[jvm] val getClassesIO         = classes.set(classBean.getLoadedClassCount.toLong)

  private def protect[A](fa: F[A])(fu: A => F[Unit]): F[Unit] =
    F.recoverWith(fa.flatMap(fu)) {
      case _ => F.unit
    }

  private def gc: Vector[F[Unit]] =
    gcBeans.map { bean =>
      val gcName = Tag.of("gc_name", bean.getName.replace(" ", "_"))
      gcCollections.set(bean.getCollectionCount, gcName)
      gcTime.set(bean.getCollectionTime, gcName)
    }

  private def buffers: Vector[F[Unit]] = bufferBeans.map { bean =>
    val beanName = Tag.of("buffer_pool", bean.getName)
    bufferPoolsBytes.set(bean.getMemoryUsed, beanName) >>
      bufferPoolsInstances.set(bean.getCount, beanName)
  }
}
