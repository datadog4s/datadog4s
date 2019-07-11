name := "datadog-scala-metrics"

version := "0.1"

scalaVersion := "2.12.0"

libraryDependencies ++= Seq("io.micrometer" % "micrometer-core" % "1.2.0",
                            "org.typelevel" %% "cats-effect" % "1.3.1",
                            "com.datadoghq" % "java-dogstatsd-client" % "2.8")

organization := "com.avast.cloud.metrics"
version := sys.env.getOrElse("TRAVIS_TAG", "0.1-SNAPSHOT")

publishArtifact in Test := false
bintrayOrganization := Some("avast")
bintrayPackage := "cactus"
pomExtra := (
  <scm>
    <url>git@github.com:avast/
      {name.value}
      .git</url>
    <connection>scm:git:git@github.com:avast/
      {name.value}
      .git</connection>
  </scm>
    <developers>
      <developer>
        <id>avast</id>
        <name>Jan Kolena, Avast Software s.r.o.</name>
        <url>https://www.avast.com</url>
      </developer>
    </developers>
)
