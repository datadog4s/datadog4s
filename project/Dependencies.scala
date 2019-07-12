import sbt._

object Dependencies {
  object Cats {
    val core   = "org.typelevel" %% "cats-core"   % "1.6.0"
    val effect = "org.typelevel" %% "cats-effect" % "1.3.1"
  }

  object Datadog {
    val statsDClient = "com.datadoghq" % "java-dogstatsd-client" % "2.8"
  }

  object Testing {
    val scalaTest        = "org.scalatest" %% "scalatest"               % "3.0.8"
    val mockitoScalatest = "org.mockito"   %% "mockito-scala-scalatest" % "1.5.11"
  }
}
