package com.avast.datadog4cats.extension.jvm

import java.lang.management.ManagementFactory

import scala.collection.JavaConverters._
import com.sun.management._
import cats.effect.Sync
import cats.Traverse
import cats.instances.vector._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.avast.datadog4cats.api.{ MetricFactory, Tag }
import sun.management.ManagementFactoryHelper

class JvmReporter[F[_]: Sync](metricsFactory: MetricFactory[F]) {
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

  private val osBean      = ManagementFactory.getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean]
  private val memoryBean  = ManagementFactory.getMemoryMXBean
  private val runtimeBean = ManagementFactory.getRuntimeMXBean
  private val unixBean    = ManagementFactory.getOperatingSystemMXBean.asInstanceOf[UnixOperatingSystemMXBean]
  private val threadBean  = ManagementFactory.getThreadMXBean
  private val classBean   = ManagementFactory.getClassLoadingMXBean
  private val bufferBeans = ManagementFactoryHelper.getBufferPoolMXBeans.asScala.toVector
  private val gcBeans     = ManagementFactory.getGarbageCollectorMXBeans.asScala.toVector

  def collect: F[Unit] =
    Traverse[Vector].sequence(buffers) >>
      Traverse[Vector].sequence(gc) >>
      cpuLoad.set(osBean.getProcessCpuLoad) >>
      cpuTime.set(osBean.getProcessCpuTime) >>
      openFds.set(unixBean.getOpenFileDescriptorCount) >>
      heapUsed.set(memoryBean.getHeapMemoryUsage.getUsed) >>
      heapCommitted.set(memoryBean.getHeapMemoryUsage.getCommitted) >>
      heapMax.set(memoryBean.getHeapMemoryUsage.getMax) >>
      nonHeapCommited.set(memoryBean.getNonHeapMemoryUsage.getCommitted) >>
      nonHeapUsed.set(memoryBean.getNonHeapMemoryUsage.getUsed) >>
      uptime.set(runtimeBean.getUptime) >>
      threadsTotal.set(threadBean.getThreadCount) >>
      threadsDaemon.set(threadBean.getDaemonThreadCount) >>
      threadsStarted.set(threadBean.getTotalStartedThreadCount) >>
      classes.set(classBean.getLoadedClassCount)

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
