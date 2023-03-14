#!/bin/bash
dirpath="$(cd "$(dirname "$0")" && pwd)"
cd "${dirpath}" || exit

# set -e
target_dir=$(pwd)
name_prefix="rsa"
private_name_suffix="_private.pem"
public_name_suffix="_public.pem"

help() {
    echo $1
    cat <<EOF
Description:
    Generate rsa public-private key pairs used by the WeCross-Account-Manager, the private key named rsa_private.pem and the public key named rsa_public.pem

Usage:
    -d <dir>                            [Optional] generated target directory, default: the current directory
    -h                                  [Optional] Help
e.g 
    bash $0 -d WeCross-Account-Manager/conf/
EOF

    exit 0
}

LOG_WARN() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_FALT() {
    local content=${1}
    echo -e "\033[31m[FALT] ${content}\033[0m"
    exit 1
}

check_env() {
    [ ! -z "$(openssl version | grep 1.0.2)" ] || [ ! -z "$(openssl version | grep 1.1)" ] || [ ! -z "$(openssl version | grep '3.')" ] || [ ! -z "$(openssl version | grep reSSL)" ] || {
        LOG_FALT "Please install openssl!"
        #echo "download openssl from https://www.openssl.org."
        LOG_INFO "Use \"openssl version\" command to check."
        exit 1
    }
    if [ ! -z "$(openssl version | grep reSSL)" ]; then
        export PATH="/usr/local/opt/openssl/bin:$PATH"
    fi
    if [ "$(uname)" == "Darwin" ]; then
        macOS="macOS"
    fi
    if [ "$(uname -m)" != "x86_64" ]; then
        x86_64_arch="false"
    fi
}

check_name() {
    local name="$1"
    local value="$2"
    [[ "$value" =~ ^[a-zA-Z0-9._-]+$ ]] || {
        LOG_FALT "$name name [$value] invalid, it should match regex: ^[a-zA-Z0-9._-]+\$"
    }
}

file_must_exists() {
    if [ ! -f "$1" ]; then
        LOG_FALT "$1 file does not exist, please check!"
    fi
}

file_must_not_exists() {
    if [ -f "$1" ]; then
        LOG_FALT "$1 file exists, please check!"
    fi
}

dir_must_exists() {
    if [ ! -d "$1" ]; then
        LOG_FALT "$1 DIR does not exist, please check!"
    fi
}

dir_must_not_exists() {
    if [ -e "$1" ]; then
        LOG_FALT "$1 DIR exists, please clean old DIR!"
    fi
}

gen_rsa_keypair() {

    local private_name=${name_prefix}${private_name_suffix}
    local public_name=${name_prefix}${public_name_suffix}

    mkdir -p "${target_dir}"
    dir_must_exists "${target_dir}"

    file_must_not_exists "${target_dir}"/${private_name}
    file_must_not_exists "${target_dir}"/${public_name}

    local compatibility_flag=''
    if [[ ! -z "$(openssl version | grep '3.0')" ]]; then
      compatibility_flag='-traditional'
    fi

    openssl genrsa ${compatibility_flag} -out "${target_dir}"/"${private_name}" 4096
    openssl rsa -in "${target_dir}"/"${private_name}" -out "${target_dir}"/"${public_name}" -pubout

    LOG_INFO "Build rsa keypair successfully!"
}

while getopts "d:n:h" option; do
    case $option in
    d) target_dir=$OPTARG ;;
    *) help ;;
    esac
done

main() {
    check_env
    gen_rsa_keypair
}

main
