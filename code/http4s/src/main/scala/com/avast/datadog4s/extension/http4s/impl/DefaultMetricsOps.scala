package com.avast.datadog4s.extension.http4s.impl

import java.time.Duration

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import com.avast.datadog4s.api.MetricFactory
import com.avast.datadog4s.api.tag.Tagger
import com.avast.datadog4s.extension.http4s.DatadogMetricsOps.ClassifierTags
import com.avast.datadog4s.extension.http4s._
import org.http4s.metrics.{ MetricsOps, TerminationType }
import org.http4s.{ Method, Status }

private[http4s] class DefaultMetricsOps[F[_]](
  metricFactory: MetricFactory[F],
  classifierTags: ClassifierTags,
  activeConnectionsRef: Ref[F, ActiveConnections]
)(
  implicit F: Sync[F]
) extends MetricsOps[F] {
  private[this] val methodTagger          = Tagger.make[Method]("method")
  @deprecated("please use terminationTypeTagger", "0.6.3")
  private[this] val typeTagger            = Tagger.make[TerminationType]("type")
  private[this] val terminationTypeTagger = Tagger.make[TerminationType]("termination_type")
  private[this] val statusCodeTagger      = Tagger.make[Status]("status_code")
  private[this] val statusBucketTagger    = Tagger.make[String]("status_bucket")
  private[this] val activeRequests        = metricFactory.gauge.long("active_requests")

  override def increaseActiveRequests(classifier: Option[String]): F[Unit] =
    modifyActiveRequests(classifier, 0, 1)

  override def decreaseActiveRequests(classifier: Option[String]): F[Unit] =
    // if we try to decrement non existing classifier, make sure it's zero
    modifyActiveRequests(classifier, 1, -1)

  private def modifyActiveRequests(classifier: Option[String], default: Int, delta: Int): F[Unit] =
    activeConnectionsRef.modify { activeConnections =>
      val current               = activeConnections.getOrElse(classifier, default)
      val next                  = current + delta
      val nextActiveConnections = activeConnections.updated(classifier, next)
      val action = activeRequests.set(
        next.toLong,
        classifier.toList.flatMap(classifierTags): _*
      )
      (nextActiveConnections, action)
    }.flatten

  private[this] val headersTime = metricFactory.timer("headers_time")

  override def recordHeadersTime(method: Method, elapsed: Long, classifier: Option[String]): F[Unit] =
    headersTime
      .record(
        Duration.ofNanos(elapsed),
        methodTagger.tag(method) :: classifier.toList.flatMap(classifierTags): _*
      )

  private[this] val requestCount   = metricFactory.count("requests_count")
  private[this] val requestLatency = metricFactory.timer("requests_latency")
  override def recordTotalTime(method: Method, status: Status, elapsed: Long, classifier: Option[String]): F[Unit] = {
    val tags = methodTagger.tag(method) ::
      statusBucketTagger.tag(s"${status.code / 100}xx") ::
      statusCodeTagger.tag(status) :: classifier.toList.flatMap(classifierTags)
    requestCount.inc(tags: _*) >> requestLatency.record(Duration.ofNanos(elapsed), tags: _*)
  }

  private[this] val abnormalCount   = metricFactory.count("abnormal_count")
  private[this] val abnormalLatency = metricFactory.timer("abnormal_latency")
  override def recordAbnormalTermination(
    elapsed: Long,
    terminationType: TerminationType,
    classifier: Option[String]
  ): F[Unit] = {
    val terminationTpe  = terminationTypeTagger.tag(terminationType)
    val tpe  = typeTagger.tag(terminationType)
    val tags = tpe :: terminationTpe :: classifier.toList.flatMap(classifierTags)
    abnormalCount.inc(tags: _*) >> abnormalLatency.record(Duration.ofNanos(elapsed), tags: _*)
  }
}
