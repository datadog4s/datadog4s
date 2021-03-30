import BuildSupport.ScalaVersions._

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowJavaVersions := GithubActions.javaVersions
ThisBuild / githubWorkflowPublishPostamble := GithubActions.postPublish

lazy val scalaSettings = Seq(
  scalaVersion := scala213,
  scalacOptions ++= { if (isDotty.value) Seq("-source:3.0-migration") else Nil },
  crossScalaVersions := supportedScalaVersions,
  mimaPreviousArtifacts := previousStableVersion.value.map(organization.value %% name.value % _).toSet,
  libraryDependencies += (Dependencies.Testing.munit % Test),
  testFrameworks += new TestFramework("munit.Framework"),
  Compile / doc / sources := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, 0)) => Seq.empty //disable publishing of scaladoc due to a bug
      case _            => (Compile / doc / sources).value
    }
  }
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
    libraryDependencies += Dependencies.Cats.core.withDottyCompat(scalaVersion.value)
  )

lazy val common = project
  .in(file("code/common"))
  .settings(
    name := "datadog4s-common",
    scalaSettings,
    commonSettings,
    libraryDependencies += Dependencies.Cats.effect.withDottyCompat(scalaVersion.value),
    libraryDependencies += (Dependencies.Logging.logback % Test)
  )
  .dependsOn(api)

lazy val statsd = project
  .in(file("code/statsd"))
  .settings(
    name := "datadog4s-statsd",
    scalaSettings,
    commonSettings,
    libraryDependencies += Dependencies.Cats.effect.withDottyCompat(scalaVersion.value),
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
    libraryDependencies += Dependencies.Cats.effect.withDottyCompat(scalaVersion.value),
    libraryDependencies += Dependencies.Http4s.core.withDottyCompat(scalaVersion.value)
  )
  .dependsOn(api)

lazy val jvm        = project
  .in(file("code/jvm"))
  .settings(
    name := "datadog4s-jvm",
    scalaSettings,
    commonSettings,
    libraryDependencies += Dependencies.Cats.effect.withDottyCompat(scalaVersion.value),
    libraryDependencies += Dependencies.ScalaModules.collectionCompat
  )
  .dependsOn(api, common % "compile->compile;test->test")

lazy val playground = (project in file("code/playground"))
  .dependsOn(statsd)
  .settings(publish / skip := true, name := "datadog4s-playground", commonSettings, scalaSettings)

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
      exclude ("org.scala-lang.modules", "scala-collection-compat_2.13") // we use 3.0.0 version of scala-collection-compat
  )
  .settings(publish / skip := true)
  .settings(BuildSupport.micrositeSettings: _*)
  .dependsOn(api, statsd, http4s, jvm)

addCommandAlias(
  "checkAll",
  //"; scalafmtSbtCheck; scalafmtCheckAll; coverage; +test; coverageReport; doc; site/makeMdoc"
  "; scalafmtSbtCheck; scalafmtCheckAll; +test; doc; site/makeMdoc"
)

addCommandAlias("fixAll", "; scalafmtSbt; scalafmtAll")
