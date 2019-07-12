package com.avast.cloud.metrics.datadog

package object api {
  type Tag = String
  object Tag {
    def of(k: String, v: String): Tag = s"$k:$v"
  }
}
