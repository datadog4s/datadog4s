import CompilerSettings._

lazy val scala212               = "2.12.11"
lazy val scala213               = "2.13.1"
lazy val supportedScalaVersions = List(scala212, scala213)

lazy val scalaSettings = Seq(
  scalaVersion := scala212,
  scalacOptions ++= scalacOptionsFor(scalaVersion.value),
  scalacOptions.in(Compile, console) ~= filterConsoleScalacOptions,
  scalacOptions.in(Test, console) ~= filterConsoleScalacOptions,
  crossScalaVersions := supportedScalaVersions,
  mimaPreviousArtifacts := previousStableVersion.value.map(organization.value %% name.value % _).toSet,
  libraryDependencies ++= Seq(
    Dependencies.Testing.scalaTest        % Test,
    Dependencies.Testing.mockitoScalatest % Test
  )
)

lazy val commonSettings = Seq(
  sonatypeProfileName := "com.avast",
  organization := "com.avast.cloud",
  homepage := Some(url("https://github.com/avast/datadog4s")),
  licenses := List("MIT" -> url(s"https://github.com/avast/datadog4s/blob/${version.value}/LICENSE")),
  description := "Library for datadog app monitoring",
  developers := List(
    Developer(
      "tomasherman",
      "Tomas Herman",
      "hermant@avast.com",
      url("https://tomasherman.cz")
    )
  ),
  publishArtifact in Test := false,
  testOptions += Tests.Argument(TestFrameworks.JUnit)
)

lazy val global = project
  .in(file("."))
  .settings(name := "datadog4s")
  .settings(commonSettings)
  .settings(scalaSettings)
  .aggregate(api, statsd, http4s, jvm, site, common)
  .dependsOn(api, statsd, http4s, jvm)
  .disablePlugins(MimaPlugin)

lazy val api = project
  .in(file("code/api"))
  .settings(
    name := "datadog4s-api",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.core
    )
  )

lazy val common = project
  .in(file("code/common"))
  .settings(
    name := "datadog4s-inmemory",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Testing.scalaTest % Test
    )
  )
  .dependsOn(api)

lazy val statsd = project
  .in(file("code/statsd"))
  .settings(
    name := "datadog4s-statsd",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Datadog.statsDClient
    )
  )
  .dependsOn(api)

lazy val http4s = project
  .in(file("code/http4s"))
  .settings(
    name := "datadog4s-http4s",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Silencer.lib,
      Dependencies.Silencer.plugin
    ),
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case _ =>
          libraryDependencies.value ++ Seq(
            Dependencies.Http4s.core212
          )
      }
    }
  )
  .dependsOn(api)

lazy val jvm = project
  .in(file("code/jvm"))
  .settings(
    name := "datadog4s-jvm",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.ScalaModules.collectionCompat
    )
  )
  .dependsOn(api, common % "compile->compile;test->test")

lazy val site = (project in file("site"))
  .settings(scalaSettings)
  .settings(commonSettings)
  .disablePlugins(MimaPlugin)
  .enablePlugins(
    MdocPlugin,
    MicrositesPlugin,
    SiteScaladocPlugin,
    SiteScaladocPlugin,
    ScalaUnidocPlugin
  )
  .settings(
    libraryDependencies ++= Seq(Dependencies.Mdoc.libMdoc)
  )
  .settings(publish / skip := true)
  .settings(BuildSupport.micrositeSettings: _*)
  .dependsOn(api, statsd, http4s, jvm)

addCommandAlias(
  "checkAll",
  "; scalafmtSbtCheck; scalafmtCheckAll; doc; site/makeMdoc"
)
addCommandAlias("fixAll", "; scalafmtSbt; scalafmtAll")
