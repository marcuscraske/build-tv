#!/usr/bin/env bash

# Setup paths
PATH_CURR=$(pwd)
PATH_BASE=$(dirname "${PATH_CURR}")

echo "Path - deploy:          ${PATH_CURR}"
echo "Path - base:            ${PATH_BASE}"

PATH_SHARED_CONFIG="${PATH_BASE}/config"
echo "Path - shared config:   ${PATH_SHARED_CONFIG}"

PATH_NEOPIXEL_WS281X_LIB="${PATH_BASE}/neopixel-ws281x-lib"
echo "Path - ws281x library:  ${PATH_NEOPIXEL_WS281X_LIB}"

PATH_NEOPIXEL_WS281X_DAEMON="${PATH_BASE}/neopixel-ws281x-daemon"
echo "Path - ws281x daemon:   ${PATH_NEOPIXEL_WS281X_DAEMON}"

PATH_BUILDTV="${PATH_BASE}/build-tv-daemon"
echo "Path - build TV daemon: ${PATH_BUILDTV}"

PATH_SCREEN="${PATH_BASE}/screen-daemon"
echo "Path - screem daemon: ${PATH_SCREEN}"

# Build extra vars
EXTRA_VARS+="shared_config=\"${PATH_SHARED_CONFIG}\" "
EXTRA_VARS+="ws281x_lib=\"${PATH_NEOPIXEL_WS281X_LIB}\" "
EXTRA_VARS+="ws281x_daemon=\"${PATH_NEOPIXEL_WS281X_DAEMON}\" "
EXTRA_VARS+="build_tv_daemon=\"${PATH_BUILDTV}\" "
EXTRA_VARS+="screen_daemon=\"${PATH_SCREEN}\" "

# Run deployment
ansible-playbook deploy.yml -v -i hosts_pi --extra-vars "${EXTRA_VARS}" --tags "config,neopixel-lib,neopixel-daemon,build-tv-daemon,screen-daemon"
