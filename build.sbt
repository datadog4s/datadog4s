lazy val scalaSettings = Seq(
  scalaVersion := "2.12.8",
  scalacOptions += "-deprecation",
  scalacOptions += "-unchecked",
  scalacOptions += "-feature",
  scalacOptions += "-language:higherKinds",
  crossScalaVersions := Seq("2.12.8"),
  libraryDependencies ++= Seq(
    Dependencies.Testing.scalaTest % Test, 
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
    statsd
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

lazy val jvm = project
  .settings(
    name := "datadog-metrics-jvm",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Datadog.statsDClient
    )
  )
  .dependsOn(api)
