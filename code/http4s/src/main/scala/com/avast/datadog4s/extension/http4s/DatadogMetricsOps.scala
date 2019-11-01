package com.avast.datadog4s.extension.http4s

import java.time.Duration

import cats.syntax.flatMap._
import cats.effect.Sync
import com.avast.datadog4s.api.tag.Tagger
import com.avast.datadog4s.api.{ MetricFactory, Tag }
import org.http4s.{ Method, Status }
import org.http4s.metrics.{ MetricsOps, TerminationType }

object DatadogMetricsOps {
  type ClassifierTags = String => List[Tag]

  val defaultClassifierTags: ClassifierTags = classifier => List(Tag.of("classifier", classifier))

  def make[F[_]](metricFactory: MetricFactory[F], classifierTags: ClassifierTags = defaultClassifierTags)(
    implicit F: Sync[F]
  ): MetricsOps[F] = new MetricsOps[F] {
    private[this] val methodTagger       = Tagger.make[Method]("method")
    private[this] val typeTagger         = Tagger.make[TerminationType]("type")
    private[this] val responseCodeTagger = Tagger.make[Status]("response_code")
    private[this] val statusBucketTagger = Tagger.make[String]("status_bucket")
    private[this] val activeRequests     = metricFactory.count("active_requests")

    override def increaseActiveRequests(classifier: Option[String]): F[Unit] =
      activeRequests.inc(classifier.toList.flatMap(classifierTags): _*)

    override def decreaseActiveRequests(classifier: Option[String]): F[Unit] =
      activeRequests.dec(classifier.toList.flatMap(classifierTags): _*)

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
        responseCodeTagger.tag(status) :: classifier.toList.flatMap(classifierTags)
      requestCount.inc(tags: _*) >> requestLatency.record(Duration.ofNanos(elapsed), tags: _*)
    }

    private[this] val abnormalCount   = metricFactory.count("abnormal_count")
    private[this] val abnormalLatency = metricFactory.timer("abnormal_latency")
    override def recordAbnormalTermination(
      elapsed: Long,
      terminationType: TerminationType,
      classifier: Option[String]
    ): F[Unit] = {
      val tpe  = typeTagger.tag(terminationType)
      val tags = tpe :: classifier.toList.flatMap(classifierTags)
      abnormalCount.inc(tags: _*) >> abnormalLatency.record(Duration.ofNanos(elapsed), tags: _*)
    }
  }
}
