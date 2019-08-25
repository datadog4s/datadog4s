package com.avast.datadog4cats

package object api {
  type Tag = String
  object Tag {
    def of(k: String, v: String): Tag = s"$k:$v"
  }
}
