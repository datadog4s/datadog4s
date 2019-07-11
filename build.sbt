lazy val scalaSettings = Seq(
  scalaVersion := "2.12.8",
  scalacOptions += "-deprecation",
  scalacOptions += "-unchecked",
  scalacOptions += "-feature",
  crossScalaVersions := Seq("2.12.8", "2.13.0"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.8" % "test"
  )
)

lazy val commonSettings = Seq(
  organization := "com.avast.cloud.metrics",
  version := sys.env.getOrElse("TRAVIS_TAG", "0.1-SNAPSHOT"),
  description := "Library for datadog app  monitoring",
  licenses ++= Seq("MIT" -> url(
    s"https://github.com/avast/datadog-scala-metrics/blob/${version.value}/LICENSE")),
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
  .settings(name := "datadog-scala-metrics",
            publish := {},
            publishLocal := {},
            crossScalaVersions := Nil)
  .aggregate(
    core
  )

lazy val core = project.settings(
  name := "core",
  scalaSettings,
  commonSettings,
  libraryDependencies ++= Seq("io.micrometer" % "micrometer-core" % "1.2.0",
                              "org.typelevel" %% "cats-effect" % "1.3.1",
                              "com.datadoghq" % "java-dogstatsd-client" % "2.8")
)
