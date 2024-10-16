---
layout:   docs
title:    "Install"
position: 1
---

# Installation


To start monitoring your code, first you need to add this library as a dependency to your project. This project is
composed of multiple packages to make it easy for you to pick and choose what you require.

**Available Packages:** 

| dependency name                                             |                       notes                       |
|-------------------------------------------------------------| ------------------------------------------------- |
| `"io.github.datadog4s" %% "datadog4s" % "@VERSION@"`        | all-you-can-eat ... all the available packages    |
| `"io.github.datadog4s" %% "datadog4s-api" % "@VERSION@"`    | api classes                                       |
| `"io.github.datadog4s" %% "datadog4s-statsd" % "@VERSION@"` | statsd implementation of api classes              |
| `"io.github.datadog4s" %% "datadog4s-jvm" % "@VERSION@"`    | support for monitoring JVM itself                 |
| `"io.github.datadog4s" %% "datadog4s-http4s" % "@VERSION@"` | monitoring support for [http4s][http4s] framework |


For the bare minimum, you need to add `datadog4s-api` which contains classes defining our API. You also need to add its implementation.
Currently, we only support metric delivery using StatsD in package `datadog4s` which already contains `api`. We are
going to assume you are using `sbt`.

To explore how to use imported libraries, please [read on](userguide.html)

## Note on compatibility
Datadog4s is currently released for following combinations of scala/cats versions:

|   library   |      2.12 version       |      2.13 version       | @SCALA_3_VERSION@ version |
| ----------- | ----------------------- | ----------------------- | ------------------------- |
| cats-core   | `@CATS_VERSION@`        | `@CATS_VERSION@`        | `@CATS_VERSION@`          |
| cats-effect | `@CATS_EFFECT_VERSION@` | `@CATS_EFFECT_VERSION@` | `@CATS_EFFECT_VERSION@`   |
| http4s      | `@HTTP4S_212_VERSION@`  | `@HTTP4S_213_VERSION@`  | `@HTTP4S_213_VERSION@`    |
