#!/usr/bin/env bash

# Setup paths
PATH_CURR=$(pwd)
PATH_BASE=$(dirname "${PATH_CURR}")

PATH_FILES_BASE="${PATH_CURR}/files"
PATH_FILES_OVERRIDE="${PATH_CURR}/../../rpi-config/environments"

PATH_NEOPIXEL_WS281X_LIB="${PATH_BASE}/neopixel-ws281x-lib"
PATH_NEOPIXEL_WS281X_DAEMON="${PATH_BASE}/neopixel-ws281x-daemon"
PATH_BUILDTV="${PATH_BASE}/build-tv-daemon"
PATH_SCREEN="${PATH_BASE}/screen-daemon"


# Output paths for diagnostics
echo "Path - deploy:          ${PATH_CURR}"
echo "Path - base:            ${PATH_BASE}"

echo "Path - ws281x library:  ${PATH_NEOPIXEL_WS281X_LIB}"
echo "Path - ws281x daemon:   ${PATH_NEOPIXEL_WS281X_DAEMON}"
echo "Path - build TV daemon: ${PATH_BUILDTV}"
echo "Path - screem daemon: ${PATH_SCREEN}"


# Build extra vars
EXTRA_VARS+="files_base=\"${PATH_FILES_BASE}\" "

if [[ !( -z "${PATH_FILES_OVERRIDE}") ]]; then
    EXTRA_VARS+="files_override=\"${PATH_FILES_OVERRIDE}\" "
fi

EXTRA_VARS+="ws281x_lib=\"${PATH_NEOPIXEL_WS281X_LIB}\" "
EXTRA_VARS+="ws281x_daemon=\"${PATH_NEOPIXEL_WS281X_DAEMON}\" "
EXTRA_VARS+="build_tv_daemon=\"${PATH_BUILDTV}\" "
EXTRA_VARS+="screen_daemon=\"${PATH_SCREEN}\" "

# Build tags
DEPLOY_TAGS+="setup-files,"
DEPLOY_TAGS+="setup-pi,"
DEPLOY_TAGS+="config,"
DEPLOY_TAGS+="neopixel-lib,"
DEPLOY_TAGS+="neopixel-daemon,"
DEPLOY_TAGS+="build-tv-daemon,"
DEPLOY_TAGS+="screen-daemon"

# Run deployment"
ansible-playbook deploy.yml -v -i hosts_pi --extra-vars "${EXTRA_VARS}" --tags "${DEPLOY_TAGS}"
