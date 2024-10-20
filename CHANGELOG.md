# Changelog
## 0.7.3 and going forward
*Going forward, changes will be tracked in release notes*

## 0.7.2
-   Fix publishing of root project for Scala 2.13

## 0.7.1
-   Dependency updates

## 0.7.0
-   Both scala `2.12` and `2.13` release now use http4s `0.21.0`

## 0.6.4
-   Recommended tag to use for abnormal termination type is now `abnormal_termination_type` (instead of `type)

## 0.6.3
-   Dependency update release

## 0.6.2
-   Dependency update release

## 0.6.1
-   Test release to maven-central

## 0.6.0
-   We are now publishing to maven-central instead of bintray

## 0.5.0
-   Better active requests handling in http4s

## 0.4.0
-   Renamed `response_code` tag to `status_code`
-   Added cross compilation for Scala 2.13  

## 0.3.7
-   Fixed JVM metrics

## 0.3.6
-   Removed docs as dependency of root project - we really need to make snapshots working

## 0.3.5
-   Root project now depends on subprojects

## 0.3.4
-   Another stab at fixing of publish of root project

## 0.3.3
-   Disabled fix of publishing of docs module

## 0.3.2
-   Fixed publishing of root project

## 0.3.1
-   fixed bug introduced in 0.3.0 with metric `jvm.cpu.time` not being collected properly ([issue #75](https://github.com/datadog4s/datadog4s/issues/75))

## 0.3.0
-   **breaking**: metric `jvm.nonheap.commited` renamed to `jvm.nonheap.committed` ([issue #70](https://github.com/datadog4s/datadog4s/issues/70))
-   Fixed [issue #69](https://github.com/datadog4s/datadog4s/issues/69) (windows jdk compatibility) by disabling metrics not obtainable on windows

## 0.2.5
### Version updates
-   `org.scalameta:scalafmt-core from 2.0.1 to 2.2.2`
-   `ch.epfl.scala:sbt-bloop from 1.3.2 to 1.3.5`
-   `com.datadoghq:java-dogstatsd-client from 2.8 to 2.8.1`
-   `org.scalameta:sbt-mdoc from 1.3.4 to 1.3.6`
-   `org.scalameta:sbt-scalafmt from 2.0.6 to 2.2.1`
-   `org.mockito:mockito-scala-scalatest from 1.5.18 to 1.7.0`
-   `org.scala-sbt:sbt from 1.3.2 to 1.3.3`
-   `org.http4s:http4s-core from 0.20.11 to 0.20.12`

Note: Starting from next release, dependency updates will not be tracked in the changelog unless they introduce a major change.

## 0.2.4

### Added
-   Added instances of `TagValue` for numeric types such as: `Int`, `Long`...
### Version updates
-   `scala from 2.12.9 to 2.12.10`
-   `org.scalameta:sbt-scalafmt from 2.0.4 to 2.0.5`
-   `com.typesafe:sbt-mima-plugin from 0.6.0 to 0.6.1`
-   `org.scala-sbt:sbt from 1.3.0 to 1.3.2`
-   `org.mockito:mockito-scala-scalatest from 1.5.16 to 1.5.18`
-   `org.scalameta:sbt-mdoc from 1.3.2 to 1.3.4`
-   `org.scalameta:sbt-scalafmt from 2.0.5 to 2.0.6`
## 0.2.3
### Misc
Start of a change log
