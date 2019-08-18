#!/usr/bin/env bash

set -x

sbt ++$TRAVIS_SCALA_VERSION clean scalafmtCheck test docs/mdoc &&
 if $(test ${TRAVIS_REPO_SLUG} == "avast/datadog-scala-metrics" && test ${TRAVIS_PULL_REQUEST} == "false" && test "$TRAVIS_TAG" != ""); then
   sbt +publish
 else
   exit 0 # skipping publish, it's regular build
 fi