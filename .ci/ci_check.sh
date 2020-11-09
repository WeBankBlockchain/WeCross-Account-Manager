#!/bin/bash

set -e

bash gradlew verifyGoogleJavaFormat
bash gradlew build -x test
bash gradlew test -i
bash gradlew jacocoTestReport
