import sbt._

object Dependencies {
  object Cats {
    val core   = "org.typelevel" %% "cats-core"   % "2.1.1"
    val effect = "org.typelevel" %% "cats-effect" % "2.1.2"
  }

  object Datadog {
    val statsDClient = "com.datadoghq" % "java-dogstatsd-client" % "2.9.0"
  }

  object Http4s {
    val core212 = "org.http4s" %% "http4s-core" % "0.21.1"
    val core213 = "org.http4s" %% "http4s-core" % "0.21.1"
  }

  object Testing {
    val scalaTest        = "org.scalatest" %% "scalatest"               % "3.1.1"
    val mockitoScalatest = "org.mockito"   %% "mockito-scala-scalatest" % "1.13.0"
  }

  object Mdoc {
    val libMdoc = "org.scalameta" %% "mdoc" % "2.1.4" excludeAll (ExclusionRule(
      organization = "org.slf4j"
    ))
  }

  object ScalaModules {
    val collectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.4"
  }

  object Silencer {
    val lib    = "com.github.ghik" % "silencer-lib" % "1.6.0" % Provided cross CrossVersion.full
    val plugin = compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.6.0" cross CrossVersion.full)
  }
}
