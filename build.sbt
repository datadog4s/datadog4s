import CompilerSettings._

lazy val scala212               = "2.12.9"
lazy val supportedScalaVersions = List(scala212)

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
  organization := "com.avast.cloud",
  version := sys.env.getOrElse("TRAVIS_TAG", "0.1-SNAPSHOT"),
  description := "Library for datadog app monitoring",
  licenses ++= Seq("MIT" -> url(s"https://github.com/avast/datadog4s/blob/${version.value}/LICENSE")),
  publishArtifact in Test := false,
  bintrayOrganization := Some("avast"),
  bintrayPackage := "datadog4s",
  pomExtra := (
    <scm>
      <url>git@github.com:avast/datadog4s.git</url>
      <connection>scm:git:git@github.com:avast/datadog4s.git</connection>
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
  .settings(name := "datadog4s", publish := {}, publishLocal := {}, crossScalaVersions := Nil)
  .aggregate(
    api,
    statsd,
    http4s,
    jvm,
    docs
  )

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
      Dependencies.Cats.effect,
      Dependencies.Http4s.core
    )
  )
  .dependsOn(api)

lazy val jvm = project
  .in(file("code/jvm"))
  .settings(
    name := "datadog4s-jvm",
    scalaSettings,
    commonSettings,
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect
    )
  )
  .dependsOn(api)

lazy val docs = project
  .in(file("compiled-docs"))
  .settings(commonSettings)
  .dependsOn(statsd)
  .dependsOn(http4s)
  .dependsOn(jvm)
  .enablePlugins(MdocPlugin)

skip in (publish in docs) := true
