import sbt._

object Dependencies {
  object Cats {
    val core   = "org.typelevel" %% "cats-core"   % "2.0.0"
    val effect = "org.typelevel" %% "cats-effect" % "2.0.0"
  }

  object Datadog {
    val statsDClient = "com.datadoghq" % "java-dogstatsd-client" % "2.8.1"
  }

  object Http4s {
    val core = "org.http4s" %% "http4s-core" % "0.20.15"
  }

  object Testing {
    val scalaTest        = "org.scalatest" %% "scalatest"               % "3.1.0"
    val mockitoScalatest = "org.mockito"   %% "mockito-scala-scalatest" % "1.9.0"
  }
}
