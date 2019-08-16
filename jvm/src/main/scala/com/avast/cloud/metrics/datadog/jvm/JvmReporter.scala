package com.avast.cloud.metrics.datadog.jvm

import java.lang.management.ManagementFactory

import scala.collection.JavaConverters._
import com.sun.management._
import cats.effect.Sync
import cats.Traverse
import cats.instances.vector._
import cats.syntax.flatMap._
import com.avast.cloud.metrics.datadog.api.{ MetricFactory, Tag }
import sun.management.ManagementFactoryHelper

class JvmReporter[F[_]: Sync](metricsFactory: MetricFactory[F]) {
  private val cpuLoad = metricsFactory.gauge.double("jvm.cpu.load")
  private val cpuTime = metricsFactory.gauge.long("jvm.cpu.time")

  private val openFds = metricsFactory.gauge.double("jvm.filedescriptor.open")

  private val heapUsed      = metricsFactory.gauge.double("jvm.heap.used")
  private val heapCommitted = metricsFactory.gauge.double("jvm.heap.committed")
  private val heapMax       = metricsFactory.gauge.double("jvm.heap.max")

  private val nonHeapUsed     = metricsFactory.gauge.double("jvm.nonheap.used")
  private val nonHeapCommited = metricsFactory.gauge.double("jvm.nonheap.commited")

  private val uptime = metricsFactory.gauge.double("jvm.uptime")

  private val threadsTotal   = metricsFactory.gauge.double("jvm.threads.total")
  private val threadsDaemon  = metricsFactory.gauge.double("jvm.threads.daemon")
  private val threadsStarted = metricsFactory.gauge.double("jvm.threads.started")

  private val classes = metricsFactory.gauge.double("jvm.classes.loaded")

  private val bufferPoolsInstances = metricsFactory.gauge.double("jvm.bufferpool.instances")
  private val bufferPoolsBytes     = metricsFactory.gauge.double("jvm.bufferpool.bytes")

  private val gcCollections = metricsFactory.gauge.double("jvm.gc.collections")
  private val gcTime        = metricsFactory.gauge.double("jvm.gc.time")

  private val osBean      = ManagementFactory.getOperatingSystemMXBean.asInstanceOf[OperatingSystemMXBean]
  private val memBean     = ManagementFactory.getMemoryMXBean
  private val runtimeBean = ManagementFactory.getRuntimeMXBean
  private val unixBean    = ManagementFactory.getOperatingSystemMXBean.asInstanceOf[UnixOperatingSystemMXBean]
  private val threadBean  = ManagementFactory.getThreadMXBean
  private val classBean   = ManagementFactory.getClassLoadingMXBean
  private val bufferBeans = ManagementFactoryHelper.getBufferPoolMXBeans.asScala.toVector
  private val gcBeans     = ManagementFactory.getGarbageCollectorMXBeans.asScala.toVector

  def collect: F[Unit] =
    cpuLoad.set(osBean.getProcessCpuLoad) >>
      cpuTime.set(osBean.getProcessCpuTime) >>
      openFds.set(unixBean.getOpenFileDescriptorCount) >>
      heapUsed.set(memBean.getHeapMemoryUsage.getUsed) >>
      heapCommitted.set(memBean.getHeapMemoryUsage.getCommitted) >>
      heapMax.set(memBean.getHeapMemoryUsage.getMax) >>
      nonHeapCommited.set(memBean.getNonHeapMemoryUsage.getCommitted) >>
      nonHeapUsed.set(memBean.getNonHeapMemoryUsage.getUsed) >>
      uptime.set(runtimeBean.getUptime) >>
      threadsTotal.set(threadBean.getThreadCount) >>
      threadsDaemon.set(threadBean.getDaemonThreadCount) >>
      threadsStarted.set(threadBean.getTotalStartedThreadCount) >>
      classes.set(classBean.getLoadedClassCount) >>
      Traverse[Vector].sequence(buffers) >>
      Traverse[Vector].sequence(gc)

  private def gc: Vector[F[Unit]] =
    gcBeans.map { bean =>
      val gcName = Tag.of("gcName", bean.getName.replace(" ", "_"))
      gcCollections.set(bean.getCollectionCount, gcName)
      gcTime.set(bean.getCollectionTime, gcName)
    }

  private def buffers: Vector[F[Unit]] = bufferBeans.map { bean =>
    val beanName = Tag.of("bufferPool", bean.getName)
    bufferPoolsBytes.set(bean.getMemoryUsed, beanName) >>
      bufferPoolsInstances.set(bean.getCount, beanName)
  }

}
