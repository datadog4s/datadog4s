# Datadog Scala Metrics [![Build Status](https://travis-ci.org/avast/datadog4s.svg?branch=master)](https://travis-ci.org/avast/datadog4s) [![Download](https://api.bintray.com/packages/avast/maven/datadog4s/images/download.svg)](https://bintray.com/avast/maven/datadog4s/_latestVersion) <img height="40" src="https://typelevel.org/cats/img/cats-badge-tiny.png" align="right"/>


Toolkit for monitoring applications written in functional Scala using Datadog.

Goal of this project is to make great monitoring as easy as possible. 

In addition to basic monitoring utilities, we also provide bunch of plug-and-play modules that do monitoring for you. Currently those are:
- JVM monitoring
- Http4s monitoring

## Quick start
Latest version: [ ![Download](https://api.bintray.com/packages/avast/maven/datadog4s/images/download.svg) ](https://bintray.com/avast/maven/datadog4s/_latestVersion)

To add all packages, add to `build.sbt`:
```scala
libraryDependencies += "com.avast" %% "datadog4s" % "latestVersion" 
```

Or pick and choose from the available packages:

| dependency name | notes |
|--------------|-------| 
| `"com.avast" %% "datadog4s" % "latestVersion"`  | all-you-can-eat ... all the available packages |
| `"com.avast" %% "datadog4s-api" % "latestVersion"`  | api classes |
| `"com.avast" %% "datadog4s-statsd" % "latestVersion"`  | statsd implementation of api classes |
| `"com.avast" %% "datadog4s-jvm" % "latestVersion"`  | support for monitoring JVM itself |
| `"com.avast" %% "datadog4s-http4s" % "latestVersion"`  | monitoring support for [http4s][http4s] framework |

## Documentation
For documentation, please read our [user guide](./docs/userguide.md).


[http4s]: https://http4s.org