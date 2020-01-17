---
layout: home
title:  "Home"
section: "home"
---

Toolkit for monitoring applications written in functional Scala using Datadog.

Goal of this project is to make great monitoring as easy as possible. 

In addition to basic monitoring utilities, we also provide bunch of plug-and-play modules that do monitoring for you. Currently those are:
- JVM monitoring
- Http4s monitoring

## Quick start
Latest version: [![Download](https://img.shields.io/maven-central/v/com.avast.cloud/datadog4s-api_2.13)](https://search.maven.org/search?q=g:com.avast.cloud%20datadog4s)

To add all packages, add to `build.sbt`:

```scala
libraryDependencies += "com.avast.cloud" %% "datadog4s" % "@VERSION@" 
```

Or pick and choose from the available packages:

| dependency name | notes |
|--------------|-------| 
| `"com.avast.cloud" %% "datadog4s" % "@VERSION@"`  | all-you-can-eat ... all the available packages |
| `"com.avast.cloud" %% "datadog4s-api" % "@VERSION@"`  | api classes |
| `"com.avast.cloud" %% "datadog4s-statsd" % "@VERSION@"`  | statsd implementation of api classes |
| `"com.avast.cloud" %% "datadog4s-jvm" % "@VERSION@"`  | support for monitoring JVM itself |
| `"com.avast.cloud" %% "datadog4s-http4s" % "@VERSION@"`  | monitoring support for [http4s][http4s] framework |

# User guide

For documentation, please read our [user guide](quickstart.html).
