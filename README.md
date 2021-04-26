# Datadog Scala Metrics ![Build](https://github.com/avast/datadog4s/workflows/Build/badge.svg?branch=master) [![Download](https://img.shields.io/maven-central/v/com.avast.cloud/datadog4s-api_2.13)](https://search.maven.org/search?q=g:com.avast.cloud%20datadog4s) <img height="40" src="https://typelevel.org/cats/img/cats-badge-tiny.png" align="right"/>

Toolkit for monitoring applications written in functional Scala using Datadog and Cats.

Goal of this project is to make great monitoring as easy as possible. 

In addition to basic monitoring utilities, we also provide bunch of plug-and-play modules that do monitoring for you. Currently those are:
-   JVM monitoring
-   Http4s monitoring

## Quick start
Latest version: [![Download][shield-url]][shield-link-url]

To add all packages, add to `build.sbt`:

```scala
libraryDependencies += "com.avast.cloud" %% "datadog4s" % "latestVersion" 
```

Or pick and choose from the available packages:

| dependency name                                             | notes                                             |
|-------------------------------------------------------------|---------------------------------------------------| 
| `"com.avast.cloud" %% "datadog4s" % "latestVersion"`        | all-you-can-eat ... all the available packages    |
| `"com.avast.cloud" %% "datadog4s-api" % "latestVersion"`    | api classes                                       |
| `"com.avast.cloud" %% "datadog4s-statsd" % "latestVersion"` | statsd implementation of api classes              |
| `"com.avast.cloud" %% "datadog4s-jvm" % "latestVersion"`    | support for monitoring JVM itself                 |
| `"com.avast.cloud" %% "datadog4s-http4s" % "latestVersion"` | monitoring support for [http4s][http4s] framework |

### Cats-Effect 2 vs Cats-Effect3
For more information about compatibility with CE2 or CE3 please see the [compatibility matrix][compatibility-matrix]

## Documentation
For documentation, please read our [user guide][user-guide]

[shield-url]: https://img.shields.io/maven-central/v/com.avast.cloud/datadog4s-api_2.13
[shield-link-url]: https://search.maven.org/search?q=g:com.avast.cloud%20datadog4s
[http4s]: https://http4s.org
[user-guide]: https://avast.github.io/datadog4s/userguide.html
[compatibility-matrix]: https://avast.github.io/datadog4s/index.html#compatibility-matrix