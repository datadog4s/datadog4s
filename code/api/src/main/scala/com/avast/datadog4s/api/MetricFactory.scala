package com.avast.datadog4s.api

import com.avast.datadog4s.api.MetricFactory.TimerMode.HistogramTimer
import com.avast.datadog4s.api.MetricFactory.TimerMode
import com.avast.datadog4s.api.metric.{ Count, Timer, UniqueSet }

trait MetricFactory[F[_]] {
  def histogram: HistogramFactory[F]
  def distribution: DistributionFactory[F]
  def gauge: GaugeFactory[F]
  def timer(aspect: String, sampleRate: Option[Double] = None, timerMode: TimerMode = HistogramTimer: TimerMode): Timer[F]
  def count(aspect: String, sampleRate: Option[Double] = None): Count[F]
  def uniqueSet(aspect: String): UniqueSet[F]

  def withTags(tags: Tag*): MetricFactory[F]
  def withScope(name: String): MetricFactory[F]
  def withTimerMode(timerMode: TimerMode): MetricFactory[F]
}

object MetricFactory {
  sealed trait TimerMode
  object TimerMode {
    case object HistogramTimer    extends TimerMode
    case object DistributionTimer extends TimerMode
  }
}
