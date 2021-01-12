#!/bin/bash
dirpath="$(cd "$(dirname "$0")" && pwd)"
cd ${dirpath}

rm -f start.out

APP_NAME=com.webank.wecross.account.service.Application
APPS_FOLDER=$(pwd)/apps

wecross_pid=$(ps -ef | grep ${APP_NAME} | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}')
if [ -z ${wecross_pid} ]; then
    echo -e "\033[31mWeCross-Account-Manager isn't running \033[0m"
    exit 0
fi

ps -ef | grep ${APP_NAME} | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}' | xargs kill -9

try_times=10
i=0
while [ $i -lt ${try_times} ]
do
    wecross_pid=$(ps -ef | grep ${APP_NAME} | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}')
    if [ -z ${wecross_pid} ]; then
        echo -e "\033[32mStop WeCross-Account-Manager successfully \033[0m"
        exit 0
    fi
    sleep 0.5

    ((i=i+1))
done

echo -e "\033[31mExceed waiting time. Please try again to stop WeCross \033[0m"
exit 1
