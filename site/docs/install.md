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

To pick the right version of datadog4s for your circumstance, please see the compatibility tables for cats-effect2 and cats-effect3:

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
