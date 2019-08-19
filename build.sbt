lazy val scala212               = "2.12.8"
lazy val supportedScalaVersions = List(scala212)

lazy val scalaSettings = Seq(
  scalaVersion := scala212,
  scalacOptions += "-deprecation",
  scalacOptions += "-unchecked",
  scalacOptions += "-feature",
  scalacOptions += "-language:higherKinds",
  crossScalaVersions := supportedScalaVersions,
  libraryDependencies ++= Seq(
    Dependencies.Testing.scalaTest        % Test,
    Dependencies.Testing.mockitoScalatest % Test
  )
)

lazy val commonSettings = Seq(
  organization := "com.avast.cloud",
  version := sys.env.getOrElse("TRAVIS_TAG", "0.1-SNAPSHOT"),
  description := "Library for datadog app monitoring",
  licenses ++= Seq("MIT" -> url(s"https://github.com/avast/datadog-scala-metrics/blob/${version.value}/LICENSE")),
  publishArtifact in Test := false,
  bintrayOrganization := Some("avast"),
  bintrayPackage := "datadog-scala-metrics",
  pomExtra := (
    <scm>
      <url>git@github.com:avast/datadog-scala-metrics.git</url>
      <connection>scm:git:git@github.com:avast/datadog-scala-metrics.git</connection>
    </scm>
      <developers>
        <developer>
          <id>avast</id>
          <name>Tomas Herman, Avast Software s.r.o.</name>
          <url>https://www.avast.com</url>
        </developer>
      </developers>
  ),
  publishArtifact in Test := false,
  testOptions += Tests.Argument(TestFrameworks.JUnit)
)

lazy val global = project
  .in(file("."))
  .settings(name := "datadog-metrics", publish := {}, publishLocal := {}, crossScalaVersions := Nil)
  .aggregate(
    api,
    statsd,
    jvm,
    docs
  )

lazy val api = project.settings(
  name := "datadog-metrics-api",
  scalaSettings,
  commonSettings,
  libraryDependencies ++= Seq(
    Dependencies.Cats.core
  )
)

lazy val statsd = project
  .settings(
    name := "datadog-metrics-statsd",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Datadog.statsDClient
    )
  )
  .dependsOn(api)

lazy val http4s = project
  .settings(
    name := "datadog-metrics-http4s",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Http4s.core
    )
  )
  .dependsOn(api)

lazy val jvm = project
  .settings(
    name := "datadog-metrics-jvm",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect
    )
  )
  .dependsOn(api)

lazy val docs = project
  .in(file("compiled-docs"))
  .dependsOn(statsd)
  .dependsOn(http4s)
  .dependsOn(jvm)
  .enablePlugins(MdocPlugin)

skip in (publish in docs) := true
