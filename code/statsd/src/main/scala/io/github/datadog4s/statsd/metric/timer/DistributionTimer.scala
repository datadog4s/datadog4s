package io.github.datadog4s.statsd.metric.timer

import cats.effect.{Clock, Sync}
import com.timgroup.statsd.StatsDClient
import io.github.datadog4s.api.Tag
import io.github.datadog4s.api.metric.ElapsedTime
import io.github.datadog4s.statsd.metric.TimerImpl

import java.util.concurrent.TimeUnit

class DistributionTimer[F[_]: Sync](
    clock: Clock[F],
    statsDClient: StatsDClient,
    aspect: String,
    sampleRate: Double,
    defaultTags: Seq[Tag],
    timeUnit: TimeUnit
) extends TimerImpl[F](clock) {
  override def record[T: ElapsedTime](t: T, tags: Tag*): F[Unit] =
    Sync[F].delay {
      statsDClient.recordDistributionValue(
        aspect,
        ElapsedTime[T].amount(t, timeUnit),
        sampleRate,
        (tags ++ defaultTags)*
      )
    }

}
