#!/bin/bash

set -e

DB_PASSWORD=${CI_DB_PASSWORD}

sed_i() {
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        sed -i "" $@
    else
        sed -i $@
    fi
}

sed_i "/password/s/''/'${DB_PASSWORD}'/g" src/test/resources/application.toml

bash gradlew verifyGoogleJavaFormat
bash gradlew build -x test
bash gradlew test -i
bash gradlew jacocoTestReport
