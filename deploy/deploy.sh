#!/usr/bin/env bash

# Setup paths
PATH_CURR=$(pwd)
PATH_BASE=$(dirname "${PATH_CURR}")

PATH_FILES_BASE="${PATH_CURR}/files"
PATH_FILES_OVERRIDE="${PATH_CURR}/../../rpi-config/environments"

PATH_NEOPIXEL_WS281X_LIB="${PATH_BASE}/neopixel-ws281x-lib"
PATH_LED_DAEMON="${PATH_BASE}/led-daemon"
PATH_BUILDTV_DAEMON="${PATH_BASE}/build-tv-daemon"
PATH_SCREEN_DAEMON="${PATH_BASE}/screen-daemon"
PATH_NOTIFICATION_CLIENT="${PATH_BASE}/notification-client"

# Determine inventory file to use
INVENTORY="${PATH_FILES_OVERRIDE}/hosts_inventory"

if [[ ! -f "${INVENTORY}" ]]; then
    echo "Override host inventory missing, using default inventory..."
    INVENTORY="hosts_inventory"
fi

echo "Inventory file: ${INVENTORY}"

# Output paths for diagnostics
echo "Path - deploy:                ${PATH_CURR}"
echo "Path - base:                  ${PATH_BASE}"

echo "Path - ws281x library:        ${PATH_NEOPIXEL_WS281X_LIB}"
echo "Path - led daemon:            ${PATH_LED_DAEMON}"
echo "Path - build TV daemon:       ${PATH_BUILDTV_DAEMON}"
echo "Path - screem daemon:         ${PATH_SCREEN_DAEMON}"
echo "Path - notification client:   ${PATH_NOTIFICATION_CLIENT}"


# Build extra vars
EXTRA_VARS+="files_base=\"${PATH_FILES_BASE}\" "

if [[ !( -z "${PATH_FILES_OVERRIDE}") ]]; then
    EXTRA_VARS+="files_override=\"${PATH_FILES_OVERRIDE}\" "
fi

EXTRA_VARS+="ws281x_lib=\"${PATH_NEOPIXEL_WS281X_LIB}\" "
EXTRA_VARS+="led_daemon=\"${PATH_LED_DAEMON}\" "
EXTRA_VARS+="build_tv_daemon=\"${PATH_BUILDTV_DAEMON}\" "
EXTRA_VARS+="screen_daemon=\"${PATH_SCREEN_DAEMON}\" "
EXTRA_VARS+="notification_client=\"${PATH_NOTIFICATION_CLIENT}\" "

# Build tags
if [[ -z "${1}" ]]; then
    DEPLOY_TAGS+="setup-pi,"
    DEPLOY_TAGS+="wallboard,"
    DEPLOY_TAGS+="config,"
    DEPLOY_TAGS+="neopixel-lib,"
    DEPLOY_TAGS+="led-daemon,"
    DEPLOY_TAGS+="build-tv-daemon,"
    DEPLOY_TAGS+="screen-daemon,"
    DEPLOY_TAGS+="notification-client"
    DEPLOY_TAGS+="reboot"
else
    DEPLOY_TAGS="${1}"
fi

# Run deployment"
ansible-playbook deploy.yml -v -i ${INVENTORY} --extra-vars "${EXTRA_VARS}" --tags "${DEPLOY_TAGS}"
