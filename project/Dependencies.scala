import sbt._

object Dependencies {
  object Cats {
    val core   = "org.typelevel" %% "cats-core"   % "2.1.0"
    val effect = "org.typelevel" %% "cats-effect" % "2.0.0"
  }

  object Datadog {
    val statsDClient = "com.datadoghq" % "java-dogstatsd-client" % "2.8.1"
  }

  object Http4s {
    val coreV20 = "org.http4s" %% "http4s-core" % "0.20.15"
    val coreV21 = "org.http4s" %% "http4s-core" % "0.21.0-M6"
  }

  object Testing {
    val scalaTest        = "org.scalatest" %% "scalatest"               % "3.1.0"
    val mockitoScalatest = "org.mockito"   %% "mockito-scala-scalatest" % "1.10.4"
  }

  object Silencer {
    val plugin = compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.4.4" cross CrossVersion.full)
    val lib            = "com.github.ghik" % "silencer-lib" % "1.4.4" % Provided cross CrossVersion.full

  }
}
