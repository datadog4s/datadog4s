package com.avast.datadog4s.statsd

import cats.effect.Sync
import com.avast.datadog4s.StatsDMetricFactoryConfig
import com.avast.datadog4s.api.event.Eventer
import com.avast.datadog4s.api.{ EventFactory, Tag }
import com.avast.datadog4s.statsd.event.EventerImpl
import com.timgroup.statsd.StatsDClient

class StatsDEventFactory[F[_]: Sync](statsDClient: StatsDClient, basePrefix: String, config: StatsDMetricFactoryConfig)
    extends EventFactory[F] {
  import config.defaultTags

  override def eventer: Eventer[F] = new EventerImpl[F](statsDClient, defaultTags)

  override def withTags(tags: Tag*): EventFactory[F] =
    new StatsDEventFactory[F](statsDClient, basePrefix, config.copy(defaultTags = config.defaultTags ++ tags))

  override def withScope(scope: String): EventFactory[F] =
    new StatsDEventFactory[F](statsDClient, s"$basePrefix.$scope", config)
}
