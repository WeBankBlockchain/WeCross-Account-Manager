#!/bin/bash
dirpath="$(cd "$(dirname "$0")" && pwd)"
cd ${dirpath}
export LANG='zh_CN.utf8'

APP_NAME=com.webank.wecross.account.service.Application

APPS_FOLDER=$(pwd)/apps
CLASS_PATH=$(pwd)'/apps/*:lib/*:conf:plugin/*'
WINDS_CLASS_PATH=$(pwd)'/apps/*;lib/*;conf;plugin/*'

STATUS_STARTING="Starting"
STATUS_RUNNING="Running"
STATUS_STOPPED="Stopped"

SECURIY_FILE='./.wecross.security'

LOG_INFO() {
    echo -e "\033[32m$@\033[0m"
}

LOG_ERROR() {
    echo -e "\033[31m$@\033[0m"
}

create_jvm_security()
{
  if [[ ! -f ${SECURIY_FILE} ]];then
    echo "jdk.disabled.namedCurves = " > ${SECURIY_FILE}
    # LOG_INFO "create new file ${SECURIY_FILE}"
  fi
}

show_version() {
  LOG_INFO "--------------------------------------------------------------------"
  LOG_INFO "WeCross-Account-Manager version: [" $(ls ${APPS_FOLDER} |awk '{gsub(/.jar$/,""); print}') "]"
  LOG_INFO "--------------------------------------------------------------------"
}

wecross_pid()
{
    ps -ef | grep ${APP_NAME} | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}'
}

run_wecross() 
{
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        nohup java -Dfile.encoding=UTF-8 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative="false" -Djdk.tls.namedGroups="secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448,ffdhe2048,ffdhe3072,ffdhe4096,ffdhe6144,ffdhe8192" -cp ${CLASS_PATH} ${APP_NAME} >start.out 2>&1 &
    elif [ "$(uname -s | grep MINGW | wc -l)" != "0" ]; then
        # Windows
        nohup java -Dfile.encoding=UTF-8 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative="false" -Djdk.tls.namedGroups="secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448,ffdhe2048,ffdhe3072,ffdhe4096,ffdhe6144,ffdhe8192" -cp ${WINDS_CLASS_PATH} ${APP_NAME} >start.out 2>&1 &
    else
        # GNU/Linux
        nohup java -Dfile.encoding=UTF-8 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative="false" -Djdk.tls.namedGroups="secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448,ffdhe2048,ffdhe3072,ffdhe4096,ffdhe6144,ffdhe8192" -cp ${CLASS_PATH} ${APP_NAME} >start.out 2>&1 &
    fi
}

wecross_status()
{
    if [ ! -z $(wecross_pid) ]; then
        if [ ! -z "$(grep "WeCross-Account-Manager start success" start.out)" ]; then
            echo ${STATUS_RUNNING}
        else
            echo ${STATUS_STARTING}
        fi
    else
        echo ${STATUS_STOPPED}
    fi
}

tail_log()
{
    # LOG_INFO "Debug log"
    # cat logs/debug.log
    LOG_INFO "Error log"
    tail -n 1000 logs/error.log
    LOG_INFO "Start log"
    tail -n 50 start.out
}

before_start()
{
    local status=$(wecross_status)

    case ${status} in
        ${STATUS_STARTING})
            LOG_ERROR "WeCross-Account-Manager is starting, pid is $(wecross_pid)"
            exit 0
            ;;
        ${STATUS_RUNNING})
            LOG_ERROR "WeCross-Account-Manager is running, pid is $(wecross_pid)"
            exit 0
            ;;
        ${STATUS_STOPPED})
            # do nothing
            ;;
        *)
            exit 1
            ;;
    esac
}

start()
{
    rm -f start.out
    show_version
    create_jvm_security
    run_wecross
    echo -e "\033[32mWeCross-Account-Manager booting up ..\033[0m\c"
    try_times=45
    i=0
    while [ $i -lt ${try_times} ]
    do
        sleep 1
        local status=$(wecross_status)

        case ${status} in
            ${STATUS_STARTING})
                echo -e "\033[32m.\033[0m\c"
                ;;
            ${STATUS_RUNNING})
                break
                ;;
            ${STATUS_STOPPED})
                break
                ;;
            *)
                exit 1
                ;;
        esac

        ((i=i+1))
    done
    echo ""
}

after_start()
{
    local status=$(wecross_status)

    case ${status} in
        ${STATUS_STARTING})
            kill -9 $(wecross_pid)
            LOG_ERROR "Exceed waiting time. Killed. Please try to start WeCross-Account-Manager again"
            tail_log
            exit 1
            ;;
        ${STATUS_RUNNING})
            LOG_INFO "WeCross-Account-Manager start successfully!"
            ;;
        ${STATUS_STOPPED})
            LOG_ERROR "WeCross-Account-Manager start failed"
            LOG_ERROR "See logs/error.log for details"
            tail_log
            exit 1
            ;;
        *)
            exit 1
            ;;
    esac
}

main()
{
    before_start
    start
    after_start
}

main


