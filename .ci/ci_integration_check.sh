#!/bin/bash

set -e

ROOT=$(pwd)
DIST=${ROOT}/dist/
DB_PASSWORD=${CI_DB_PASSWORD}

LOG_INFO()
{
    echo -e "\033[32m[INFO] $@\033[0m"
}

LOG_ERROR()
{
    echo -e "\033[31m[ERROR] $@\033[0m"
}

sed_i()
{
    if [ "$(uname)" == "Darwin" ]; then
    # Mac
        sed -i "" $@
    else
        sed -i $@
    fi
}

config()
{
    LOG_INFO "Create RSA keypair"
    # generate rsa_keypair
    cd ${DIST}
    bash -x create_rsa_keypair.sh -d conf/

    LOG_INFO "Configure application.toml"
    cd ${DIST}/conf
    cp -f application-sample.toml application.toml

    # sslOn = false
    sed_i 's/true/false/g' application.toml
    sed_i "/password/s/''/'${DB_PASSWORD}'/g" application.toml
    cat application.toml
    openssl genrsa -help
    ls -al *
    cat rsa*

    LOG_INFO "Configure application.properties"
    
    # spring.jpa.properties.hibernate.hbm2ddl.auto=create
    # sed_i 's/update/create/g' application.properties
    cat application.properties

    cd -
}

check_log()
{
    local error_log=${DIST}/logs/error.log
    LOG_INFO "Check log ${error_log}"
    if [ "$(grep ERROR ${error_log} |wc -l)" -ne "0" ];then
        cat ${error_log}
        LOG_ERROR "Error log is ${error_log}"
        exit 1
    fi
}

POST()
{
    local url=${1}
    local auth=${2}
    local data=${3}
    curl \
        --header "Content-Type: application/json" \
        --header "Authorization: ${auth}" \
        --request POST \
        --data ${data} \
        ${url}
}

GET()
{
    local url=${1}
    local auth=${2}
    local data=${3}
    curl \
        --header "Content-Type: application/json" \
        --header "Authorization: ${auth}" \
        --request GET \
        --data ${data} \
        ${url}
}

test()
{
    POST http://localhost:8340/auth/login "" '{"version":"1","data":{"username":"org1-admin","password":"123456"}}'
}

main()
{
    bash gradlew assemble
    config
    cd ${DIST}
    bash start.sh
    check_log
    test
}

main $@