import sbt._
object Dependencies {
  object Cats {
    val core   = "org.typelevel" %% "cats-core"   % "2.6.1"
    val effect = "org.typelevel" %% "cats-effect" % "2.5.4"
  }

  object Datadog {
    val statsDClient = "com.datadoghq" % "java-dogstatsd-client" % "4.0.0"
  }

  object Http4s {
    val core = "org.http4s" %% "http4s-core" % "0.22.10"
  }

  object Testing {
    val mockitoScalatest = "org.mockito"   %% "mockito-scala-scalatest" % "1.15.1"
    val munit            = "org.scalameta" %% "munit"                   % "0.7.29"
  }

  object Logging {
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.10"
  }

  object Mdoc {
    val libMdoc = "org.scalameta" %% "mdoc" % "2.2.24" excludeAll (ExclusionRule(
      organization = "org.slf4j"
    ))
  }

  object ScalaModules {
    val collectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.4"
  }

}
