/*
rule = Datadog4s
 */
package fix

import com.avast.datadog4s.api.MetricFactory
import com.avast.datadog4s.api.Tagger
import com.avast.cloud.datadog4s.helpers.Repeated

object Datadog4sImports {
  val factory: MetricFactory = null
  val tagger: Tagger         = null
  val repeated: Repeated     = null

  // fully-qualified reference
  def make: com.avast.datadog4s.api.MetricFactory = null
}

// Self-contained stubs so the input project compiles without the datadog4s classpath.
// The rule is purely syntactic, so it rewrites these package/reference names regardless.
package com.avast.datadog4s.api {
  class MetricFactory
  class Tagger
}
package com.avast.cloud.datadog4s.helpers {
  class Repeated
}
