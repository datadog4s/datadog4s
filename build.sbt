import CompilerSettings._

lazy val scala212               = "2.12.10"
lazy val scala213               = "2.13.1"
lazy val supportedScalaVersions = List(scala212, scala213)


inThisBuild(List(
  organization := "com.avast",
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
  )
))

lazy val scalaSettings = Seq(
  scalaVersion := scala212,
  scalacOptions ++= scalacOptionsFor(scalaVersion.value),
  scalacOptions.in(Compile, console) ~= filterConsoleScalacOptions,
  scalacOptions.in(Test, console) ~= filterConsoleScalacOptions,
  crossScalaVersions := supportedScalaVersions,
  libraryDependencies ++= Seq(
    Dependencies.Testing.scalaTest        % Test,
    Dependencies.Testing.mockitoScalatest % Test
  )
)

lazy val commonSettings = Seq(
  publishArtifact in Test := false,
  testOptions += Tests.Argument(TestFrameworks.JUnit)
)

lazy val global = project
  .in(file("."))
  .settings(name := "datadog4s")
  .settings(commonSettings)
  .aggregate(api, statsd, http4s, jvm, docs)
  .dependsOn(api, statsd, http4s, jvm)

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
      Dependencies.Cats.effect
    ),
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 13 =>
          libraryDependencies.value ++ Seq(
            Dependencies.Http4s.coreV21
          )
        case Some((2, scalaMajor)) if scalaMajor >= 12 =>
          libraryDependencies.value ++ Seq(
            Dependencies.Http4s.coreV20
          )
        case _ => libraryDependencies.value
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
  .dependsOn(api)

lazy val docs = project
  .in(file("compiled-docs"))
  .settings(scalaSettings)
  .settings(commonSettings)
  .settings(
    publish / skip := true
  )
  .dependsOn(statsd)
  .dependsOn(http4s)
  .dependsOn(jvm)
  .enablePlugins(MdocPlugin)
