import sbt.*
object Dependencies {
  object Cats {
    val core   = "org.typelevel" %% "cats-core"   % "2.10.0"
    val effect = "org.typelevel" %% "cats-effect" % "3.5.4"
  }

  object Datadog {
    val statsDClient = "com.datadoghq" % "java-dogstatsd-client" % "4.2.0"
  }

  object Http4s {
    val core = "org.http4s" %% "http4s-core" % "0.22.15"
  }

  object Testing {
    val mockitoScalatest = "org.mockito"   %% "mockito-scala-scalatest" % "1.15.1"
    val munit            = "org.scalameta" %% "munit"                   % "0.7.29"
  }

  object Logging {
    val logback = "ch.qos.logback" % "logback-classic" % "1.4.14"
  }

  object Mdoc {
    val libMdoc = "org.scalameta" %% "mdoc" % "2.5.2" excludeAll (ExclusionRule(
      organization = "org.slf4j"
    ))
  }

  object ScalaModules {
    val collectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0"
  }

}
