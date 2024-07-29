import BuildSupport.ScalaVersions._

ThisBuild / versionScheme := Some("early-semver")

lazy val mimaSettings = Seq(
  mimaPreviousArtifacts := previousStableVersion.value.map(organization.value %% name.value % _).toSet
)

// settings only for projects that are published
lazy val publishSettings = Seq() ++ mimaSettings

lazy val scalaSettings = Seq(
  scalaVersion := scala3,
  scalacOptions := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _))  => scalacOptions.value ++ Seq("-source:future")
      case Some((2, 13)) => scalacOptions.value ++ Seq("-Xsource:3")
      case Some((2, 12)) => scalacOptions.value ++ Seq("-Xsource:3", "-Wconf:cat=unused-nowarn:s")
      case other         => scalacOptions.value
    }
  },
  crossScalaVersions := supportedScalaVersions,
  libraryDependencies += (Dependencies.Testing.munit % Test),
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val commonSettings = Seq(
  sonatypeProfileName := "com.avast",
  organization        := "com.avast.cloud",
  homepage            := Some(url("https://github.com/avast/datadog4s")),
  licenses            := List("MIT" -> url(s"https://github.com/avast/datadog4s/blob/${version.value}/LICENSE")),
  description         := "Library for datadog app monitoring",
  developers := List(
    Developer(
      "tomasherman",
      "Tomas Herman",
      "hermant@avast.com",
      url("https://tomasherman.cz")
    )
  ),
  Test / publishArtifact := false,
  testOptions += Tests.Argument(TestFrameworks.JUnit)
)

lazy val global = project
  .in(file("."))
  .settings(name := "datadog4s")
  .settings(mimaSettings)
  .settings(commonSettings)
  .settings(scalaSettings)
  .aggregate(api, statsd, http4s, jvm, site, common, playground)
  .dependsOn(api, statsd, http4s, jvm)
  .disablePlugins(MimaPlugin)

lazy val api = project
  .in(file("code/api"))
  .settings(
    name := "datadog4s-api",
    scalaSettings,
    commonSettings,
    publishSettings,
    libraryDependencies += Dependencies.Cats.core
  )

lazy val common = project
  .in(file("code/common"))
  .settings(
    name := "datadog4s-common",
    scalaSettings,
    commonSettings,
    publishSettings,
    libraryDependencies += Dependencies.Cats.effect,
    libraryDependencies += (Dependencies.Logging.logback % Test)
  )
  .dependsOn(api)

lazy val statsd = project
  .in(file("code/statsd"))
  .settings(
    name := "datadog4s-statsd",
    scalaSettings,
    commonSettings,
    publishSettings,
    libraryDependencies += Dependencies.Cats.effect,
    libraryDependencies += Dependencies.Datadog.statsDClient,
    libraryDependencies += Dependencies.ScalaModules.collectionCompat
  )
  .dependsOn(api)

lazy val http4s = project
  .in(file("code/http4s"))
  .settings(
    name := "datadog4s-http4s",
    scalaSettings,
    commonSettings,
    publishSettings,
    libraryDependencies += Dependencies.Cats.effect,
    libraryDependencies += Dependencies.Http4s.core
  )
  .dependsOn(api)

lazy val jvm = project
  .in(file("code/jvm"))
  .settings(
    name := "datadog4s-jvm",
    scalaSettings,
    commonSettings,
    publishSettings,
    libraryDependencies += Dependencies.Cats.effect,
    libraryDependencies += Dependencies.ScalaModules.collectionCompat
  )
  .dependsOn(api, common % "compile->compile;test->test")

lazy val playground = project
  .in(file("code/playground"))
  .settings(
    name := "datadog4s-playground",
    commonSettings,
    scalaSettings,
    libraryDependencies += Dependencies.ScalaModules.collectionCompat
  )
  .disablePlugins(MimaPlugin)
  .dependsOn(statsd)
  .dependsOn(jvm)

lazy val site = (project in file("site"))
  .settings(scalaSettings)
  .settings(commonSettings)
  .disablePlugins(MimaPlugin)
  .enablePlugins(
    MdocPlugin,
    MicrositesPlugin,
    SiteScaladocPlugin,
    ScalaUnidocPlugin
  )
  .settings(
    libraryDependencies += Dependencies.Mdoc.libMdoc
  )
  .settings(publish / skip := true)
  .settings(BuildSupport.micrositeSettings: _*)
  .dependsOn(api, statsd, http4s, jvm)

addCommandAlias(
  "checkAll",
  // " scalafmtSbtCheck; scalafmtCheckAll; coverage; +test; coverageReport; doc; site/makeMdoc"
  "scalafmtSbtCheck; scalafmtCheckAll; +test; doc; site/makeMdoc"
)

addCommandAlias("fixAll", "; scalafmtSbt; scalafmtAll")
