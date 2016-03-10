#!/bin/bash

set -e
export TERM=dumb # needed for Gradle: https://issues.gradle.org/browse/GRADLE-2634

pushd github
./gradlew assemble
popd

cp github/build/outputs/apk/github.apk package/dis.apk
