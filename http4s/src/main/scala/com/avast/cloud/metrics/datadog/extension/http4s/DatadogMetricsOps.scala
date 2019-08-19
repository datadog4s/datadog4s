package com.avast.cloud.metrics.datadog.extension.http4s

import java.time.Duration

import cats.syntax.flatMap._
import cats.effect.Sync
import com.avast.cloud.metrics.datadog.api.{ MetricFactory, Tag }
import org.http4s.{ Method, Status }
import org.http4s.metrics.{ MetricsOps, TerminationType }

object DatadogMetricsOps {

  type ClassifierTags = String => List[Tag]

  val defaultClassifierTags: ClassifierTags = classifier => List(Tag.of("classifier", classifier))

  def make[F[_]](metricFactory: MetricFactory[F], classifierTags: ClassifierTags = defaultClassifierTags)(
    implicit F: Sync[F]
  ): MetricsOps[F] = new MetricsOps[F] {

    private[this] val activeRequests = metricFactory.count("active_requests")

    override def increaseActiveRequests(classifier: Option[String]): F[Unit] =
      activeRequests.inc(classifier.toList.flatMap(classifierTags): _*)

    override def decreaseActiveRequests(classifier: Option[String]): F[Unit] =
      activeRequests.dec(classifier.toList.flatMap(classifierTags): _*)

    private[this] val headersTime = metricFactory.timer("headers_time")

    override def recordHeadersTime(method: Method, elapsed: Long, classifier: Option[String]): F[Unit] =
      headersTime
        .record(
          Duration.ofNanos(elapsed),
          Tag.of("method", method.name) :: classifier.toList.flatMap(classifierTags): _*
        )

    private[this] val requestCount   = metricFactory.count("requests_count")
    private[this] val requestLatency = metricFactory.timer("requests_latency")
    override def recordTotalTime(method: Method, status: Status, elapsed: Long, classifier: Option[String]): F[Unit] = {
      val tags = Tag.of("method", method.name) :: Tag
        .of("response_code", status.code.toString) :: classifier.toList.flatMap(classifierTags)
      requestCount.inc(tags: _*) >> requestLatency.record(Duration.ofNanos(elapsed), tags: _*)
    }

    private[this] val abnormalCount   = metricFactory.count("abnormal_count")
    private[this] val abnormalLatency = metricFactory.timer("abnormal_latency")
    override def recordAbnormalTermination(
      elapsed: Long,
      terminationType: TerminationType,
      classifier: Option[String]
    ): F[Unit] = {
      val tpe = terminationType match {
        case TerminationType.Abnormal => "abnormal"
        case TerminationType.Error    => "error"
        case TerminationType.Timeout  => "timeout"
      }
      val tags = Tag.of("type", tpe) :: classifier.toList.flatMap(classifierTags)
      abnormalCount.inc(tags: _*) >> abnormalLatency.record(Duration.ofNanos(elapsed), tags: _*)
    }
  }

}
