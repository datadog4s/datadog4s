package com.avast.cloud.datadog4s.inmemory

import com.avast.datadog4s.api.Tag

case class Record[A](value: A, tags: Seq[Tag])
