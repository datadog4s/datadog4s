package io.github.cloud.datadog4s.inmemory

import io.github.datadog4s.api.Tag

case class Record[A](value: A, tags: Seq[Tag])
