package io.github.datadog4s.extension.http4s.impl

import java.time.Duration
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap.*
import io.github.datadog4s.api.MetricFactory
import io.github.datadog4s.extension.http4s.DatadogMetricsOps.ClassifierTags
import io.github.datadog4s.extension.http4s.*
import io.github.datadog4s.api.metric.Timer
import io.github.datadog4s.api.tag.Tagger
import org.http4s.metrics.{MetricsOps, TerminationType}
import org.http4s.{Method, Status}

private[http4s] class DefaultMetricsOps[F[_]](
    metricFactory: MetricFactory[F],
    classifierTags: ClassifierTags,
    activeConnectionsRef: Ref[F, ActiveConnections],
    distributionBasedTimers: Boolean,
    distributionBasedCounters: Boolean
)(implicit
    F: Sync[F]
) extends MetricsOps[F] {
  private val methodTagger          = Tagger.make[Method]("method")
  private val terminationTypeTagger = Tagger.make[TerminationType]("termination_type")
  private val statusCodeTagger      = Tagger.make[Status]("status_code")
  private val statusBucketTagger    = Tagger.make[String]("status_bucket")
  private val activeRequests        = metricFactory.gauge.long("active_requests")

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
        classifier.toList.flatMap(classifierTags)*
      )
      (nextActiveConnections, action)
    }.flatten

  private val headersTime = makeTimer("headers_time")

  override def recordHeadersTime(method: Method, elapsed: Long, classifier: Option[String]): F[Unit] =
    headersTime
      .record(
        Duration.ofNanos(elapsed),
        (methodTagger.tag(method) :: classifier.toList.flatMap(classifierTags))*
      )

  private val requestCount   = makeCounter("requests_count")
  private val requestLatency = makeTimer("requests_latency")
  override def recordTotalTime(method: Method, status: Status, elapsed: Long, classifier: Option[String]): F[Unit] = {
    val tags = methodTagger.tag(method) ::
      statusBucketTagger.tag(s"${status.code / 100}xx") ::
      statusCodeTagger.tag(status) :: classifier.toList.flatMap(classifierTags)
    requestCount.inc(tags*) >> requestLatency.record(Duration.ofNanos(elapsed), tags*)
  }

  private val abnormalCount   = makeCounter("abnormal_count")
  private val abnormalLatency = makeTimer("abnormal_latency")
  override def recordAbnormalTermination(
      elapsed: Long,
      terminationType: TerminationType,
      classifier: Option[String]
  ): F[Unit] = {
    val terminationTpe = terminationTypeTagger.tag(terminationType)
    val tags           = terminationTpe :: classifier.toList.flatMap(classifierTags)
    abnormalCount.inc(tags*) >> abnormalLatency.record(Duration.ofNanos(elapsed), tags*)
  }

  private def makeTimer(aspect: String): Timer[F] =
    if (distributionBasedTimers) {
      metricFactory.timer.distribution(aspect)
    } else {
      metricFactory.timer.histogram(aspect)
    }

  private def makeCounter(aspect: String): GenericCounter[F] =
    if (distributionBasedCounters) {
      new GenericCounter.DistributionCounter(metricFactory, aspect)
    } else {
      new GenericCounter.CountWrapper(metricFactory, aspect)
    }
}
