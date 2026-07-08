package fix

import scalafix.v1._
import scala.meta._

/** Rewrites references to the pre-migration `com.avast[.cloud].datadog4s` packages to the current `io.github.datadog4s`
  * packages.
  *
  * datadog4s moved its group id from `com.avast.cloud` to `io.github.datadog4s` (see
  * https://github.com/DataDog4s/datadog4s/pull/1029) and renamed its packages to match:
  *
  *   - `com.avast.cloud.datadog4s.*` -> `io.github.datadog4s.*`
  *   - `com.avast.datadog4s.*` -> `io.github.datadog4s.*`
  *
  * This is a purely syntactic prefix rename, so no SemanticDB is required.
  */
class Datadog4s extends SyntacticRule("Datadog4s") {

  // Longest prefix first: `com.avast.cloud.datadog4s` must be tested before `com.avast.datadog4s`.
  private val renames: List[(String, String)] = List(
    "com.avast.cloud.datadog4s" -> "io.github.datadog4s",
    "com.avast.datadog4s"       -> "io.github.datadog4s"
  )

  override def fix(implicit doc: SyntacticDocument): Patch =
    doc.tree.collect {
      // `import com.avast.datadog4s.api.Tagger` / `import com.avast.cloud.datadog4s.helpers._`
      case importer: Importer =>
        patchRef(importer.ref)

      // Fully-qualified usages such as `com.avast.datadog4s.api.MetricFactory.make(...)`.
      // Only rewrite the outermost qualified reference; nested `Term.Select`s are subtrees of it.
      // Skip refs that are:
      //   - inside a larger `Term.Select` (handled by the outer one),
      //   - an `Importer` ref (handled by the `Importer` case above), or
      //   - a `package com.avast.datadog4s...` declaration (the rule targets consumer code).
      case select: Term.Select if !select.parent.exists(p => p.is[Term.Select] || p.is[Importer] || p.is[Pkg]) =>
        patchRef(select)
    }.asPatch

  /** If `ref` is a qualified reference beginning with one of the renamed prefixes, replaces the whole reference with
    * the rewritten one. Uses the reference's own syntax so the trailing path (`.api.Tagger`, `.helpers`, ...) is
    * preserved exactly.
    */
  private def patchRef(ref: Tree): Patch = {
    val syntax = ref.syntax
    renames
      .collectFirst {
        case (before, after) if syntax == before || syntax.startsWith(before + ".") =>
          Patch.replaceTree(ref, after + syntax.substring(before.length))
      }
      .getOrElse(Patch.empty)
  }
}
