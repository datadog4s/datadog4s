import com.typesafe.sbt.site.SitePlugin.autoImport._
import mdoc.MdocPlugin.autoImport._
import microsites.MicrositesPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtunidoc.ScalaUnidocPlugin.autoImport._
import sbtdynver.DynVerPlugin.autoImport._

object BuildSupport {
  lazy val micrositeSettings = Seq(
    micrositeCompilingDocsTool := WithMdoc,
    micrositeName := "datadog4s",
    micrositeDescription := "Great monitoring made easy",
    micrositeAuthor := "Tomas Herman",
    micrositeGithubOwner := "avast",
    micrositeGithubRepo := "datadog4s",
    micrositeUrl := "https://avast.github.io",
    micrositeDocumentationUrl := "api/latest/com/avast/datadog4s/",
    micrositeBaseUrl := "/datadog4s",
    micrositeFooterText := None,
    micrositeGitterChannel := false,
    micrositeTheme := "pattern",
    fork in mdoc := true,
    mdocIn := file("site") / "docs",
    mdocVariables := Map("VERSION" -> previousStableVersion.value.getOrElse("latestVersion")),
    mdocAutoDependency := false,
    micrositeDataDirectory := file("site"),
    siteSubdirName in ScalaUnidoc := "api/latest",
    addMappingsToSiteDir(
      mappings in (ScalaUnidoc, packageDoc),
      siteSubdirName in ScalaUnidoc
    )
  )
}
