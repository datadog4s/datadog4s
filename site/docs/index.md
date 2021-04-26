---
layout:   home
title:    "Home"
section:  "home"
---

Toolkit for monitoring applications written in functional Scala using Datadog.

Goal of this project is to make great monitoring as easy as possible.

In addition to basic monitoring utilities, we also provide bunch of plug-and-play modules that do monitoring for you.
Currently, those are:

- JVM monitoring
- Http4s monitoring

## Quick start

Latest
version: [![Download](https://img.shields.io/maven-central/v/com.avast.cloud/datadog4s-api_2.13)](https://search.maven.org/search?q=g:com.avast.cloud%20datadog4s)

To add all packages, add to `build.sbt`:

```scala
libraryDependencies += "com.avast.cloud" %% "datadog4s" % "@VERSION@" 
```

Or pick and choose from the available packages:

| dependency name                                         | notes                                             |
| ------------------------------------------------------- | ------------------------------------------------- |
| `"com.avast.cloud" %% "datadog4s" % "@VERSION@"`        | all-you-can-eat ... all the available packages    |
| `"com.avast.cloud" %% "datadog4s-api" % "@VERSION@"`    | api classes                                       |
| `"com.avast.cloud" %% "datadog4s-statsd" % "@VERSION@"` | statsd implementation of api classes              |
| `"com.avast.cloud" %% "datadog4s-jvm" % "@VERSION@"`    | support for monitoring JVM itself                 |
| `"com.avast.cloud" %% "datadog4s-http4s" % "@VERSION@"` | monitoring support for [http4s][http4s] framework |

## Compatibility

Datadog4s is released for both cats-effect2 and scala 2.12, 2.13 and @SCALA_3_VERSION@. To choose the right version, see
the compatibility matrix:

### _cats-effect **3.x.x**_:

| library                       | 2.12 version           | 2.13 version           | @SCALA_3_VERSION@ version |
| ----------------------------- | ---------------------- | ---------------------- | ------------------------- |
| recommended datadog4s version | `@CE3_LATEST_VERSION@` | `@CE3_LATEST_VERSION@` | `@CE3_LATEST_VERSION@`    |
| http4s version                | `@HTTP4S_VERSION@`     | `@HTTP4S_VERSION@`     | `@HTTP4S_VERSION@`        |

### _cats-effect **2.x.x**_:

| library                       | 2.12 version           | 2.13 version           | @SCALA_3_VERSION@ version |
| ----------------------------- | ---------------------- | ---------------------- | ------------------------- |
| recommended datadog4s version | `@CE2_LATEST_VERSION@` | `@CE2_LATEST_VERSION@` | `@CE2_LATEST_VERSION@`    |
| http4s version                | `@HTTP4S_CE2_VERSION@` | `@HTTP4S_CE2_VERSION@` | `@HTTP4S_CE2_VERSION@`    |

# User guide

For the documentation, please read our [user guide](userguide.html).


[http4s]: http://http4s.org 