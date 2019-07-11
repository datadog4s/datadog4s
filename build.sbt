name := "metrics-ng"

version := "0.1"

scalaVersion := "2.12.0"

libraryDependencies ++= Seq("io.micrometer" % "micrometer-core" % "1.2.0",
                            "org.typelevel" %% "cats-effect" % "1.3.1",
  "com.datadoghq" % "java-dogstatsd-client" % "2.8")
