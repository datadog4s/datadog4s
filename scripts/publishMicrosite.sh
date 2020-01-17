#!/bin/bash
set -e

git config --global user.email "travis-job@travis-ci.com"
git config --global user.name "Travis CI"
git config --global push.default simple

sbt site/publishMicrosite
