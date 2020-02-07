---
layout:   home
title:    "Home"
section:  "home"
---

Toolkit for monitoring applications written in functional Scala using Datadog.

Goal of this project is to make great monitoring as easy as possible. 

In addition to basic monitoring utilities, we also provide bunch of plug-and-play modules that do monitoring for you. Currently those are:
-   JVM monitoring
-   Http4s monitoring

## Quick start
Latest version: [![Download](https://img.shields.io/maven-central/v/com.avast.cloud/datadog4s-api_2.13)](https://search.maven.org/search?q=g:com.avast.cloud%20datadog4s)

To add all packages, add to `build.sbt`:

```scala
libraryDependencies += "com.avast.cloud" %% "datadog4s" % "@VERSION@" 
```

Or pick and choose from the available packages:

|                     dependency name                     |                       notes                       |
| ------------------------------------------------------- | ------------------------------------------------- |
| `"com.avast.cloud" %% "datadog4s" % "@VERSION@"`        | all-you-can-eat ... all the available packages    |
| `"com.avast.cloud" %% "datadog4s-api" % "@VERSION@"`    | api classes                                       |
| `"com.avast.cloud" %% "datadog4s-statsd" % "@VERSION@"` | statsd implementation of api classes              |
| `"com.avast.cloud" %% "datadog4s-jvm" % "@VERSION@"`    | support for monitoring JVM itself                 |
| `"com.avast.cloud" %% "datadog4s-http4s" % "@VERSION@"` | monitoring support for [http4s][http4s] framework |

## Comaptibility
Datadog4s is currently released for both scala 2.12 and scala 2.13. It is using following versions of libraries:

|   library   |      2.12 version       |      2.13 version       |
| ----------- | ----------------------- | ----------------------- |
| cats-core   | `@CATS_VERSION@`        | `@CATS_VERSION@`        |
| cats-effect | `@CATS_EFFECT_VERSION@` | `@CATS_EFFECT_VERSION@` |
| http4s      | `@HTTP4S_212_VERSION@`  | `@HTTP4S_213_VERSION@`  |

# User guide

For documentation, please read our [user guide](userguide.html).
