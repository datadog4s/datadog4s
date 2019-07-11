#!/usr/bin/env bash
sbt ++$TRAVIS_SCALA_VERSION test &&
 if $(test ${TRAVIS_REPO_SLUG} == "avast/datadog-scala-metrics" && test ${TRAVIS_PULL_REQUEST} == "false" && test "$TRAVIS_TAG" != ""); then
   sbt +publish
 else
   exit 0 # skipping publish, it's regular build
 fi