#!/usr/bin/env bash

# Fancy script title
IFS='' read -r -d '' SCRIPT_HEADER <<'EOF'
 █████╗ ██╗   ██╗████████╗ ██████╗ ███╗   ███╗ █████╗ ████████╗███████╗██████╗     ██████╗ ██╗    ██████╗ ███████╗██████╗ ██╗      ██████╗ ██╗   ██╗███╗   ███╗███████╗███╗   ██╗████████╗
██╔══██╗██║   ██║╚══██╔══╝██╔═══██╗████╗ ████║██╔══██╗╚══██╔══╝██╔════╝██╔══██╗    ██╔══██╗██║    ██╔══██╗██╔════╝██╔══██╗██║     ██╔═══██╗╚██╗ ██╔╝████╗ ████║██╔════╝████╗  ██║╚══██╔══╝
███████║██║   ██║   ██║   ██║   ██║██╔████╔██║███████║   ██║   █████╗  ██║  ██║    ██████╔╝██║    ██║  ██║█████╗  ██████╔╝██║     ██║   ██║ ╚████╔╝ ██╔████╔██║█████╗  ██╔██╗ ██║   ██║
██╔══██║██║   ██║   ██║   ██║   ██║██║╚██╔╝██║██╔══██║   ██║   ██╔══╝  ██║  ██║    ██╔═══╝ ██║    ██║  ██║██╔══╝  ██╔═══╝ ██║     ██║   ██║  ╚██╔╝  ██║╚██╔╝██║██╔══╝  ██║╚██╗██║   ██║
██║  ██║╚██████╔╝   ██║   ╚██████╔╝██║ ╚═╝ ██║██║  ██║   ██║   ███████╗██████╔╝    ██║     ██║    ██████╔╝███████╗██║     ███████╗╚██████╔╝   ██║   ██║ ╚═╝ ██║███████╗██║ ╚████║   ██║
╚═╝  ╚═╝ ╚═════╝    ╚═╝    ╚═════╝ ╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚══════╝╚═════╝     ╚═╝     ╚═╝    ╚═════╝ ╚══════╝╚═╝     ╚══════╝ ╚═════╝    ╚═╝   ╚═╝     ╚═╝╚══════╝╚═╝  ╚═══╝   ╚═╝
EOF

echo -e "\033[1;31m
${SCRIPT_HEADER}
\033[1;32m
"


# Define paths
PATH_CURR=$(pwd)
PATH_BASE=$(dirname "${PATH_CURR}")

PATH_FILES_BASE="${PATH_CURR}/files"
PATH_FILES_OVERRIDE="${PATH_CURR}/../../rpi-config/environments"

PATH_NEOPIXEL_WS281X_LIB="${PATH_BASE}/neopixel-ws281x-lib"
PATH_LED_DAEMON="${PATH_BASE}/led-daemon"
PATH_BUILDTV_DAEMON="${PATH_BASE}/build-tv-daemon"
PATH_SYSTEM_DAEMON="${PATH_BASE}/system-daemon"
PATH_REMOTE_DAEMON="${PATH_BASE}/system-daemon"
PATH_NOTIFICATION_CLIENT="${PATH_BASE}/notification-client"


# Determine inventory file to use
INVENTORY="${PATH_FILES_OVERRIDE}/hosts_inventory"

if [[ ! -f "${INVENTORY}" ]]; then
    echo "Override host inventory missing, using default inventory..."
    echo ""
    INVENTORY="hosts_inventory"
fi

echo "Inventory file:"
echo "${INVENTORY}"
echo ""


# Output paths for diagnostics
echo "Path - deploy:                ${PATH_CURR}"
echo "Path - base:                  ${PATH_BASE}"

echo "Path - ws281x library:        ${PATH_NEOPIXEL_WS281X_LIB}"
echo "Path - led daemon:            ${PATH_LED_DAEMON}"
echo "Path - build TV daemon:       ${PATH_BUILDTV_DAEMON}"
echo "Path - system daemon:         ${PATH_SYSTEM_DAEMON}"
echo "Path - remote daemon:         ${PATH_REMOTE_DAEMON}"
echo "Path - notification client:   ${PATH_NOTIFICATION_CLIENT}"


# Build extra vars
EXTRA_VARS+="files_base=\"${PATH_FILES_BASE}\" "

if [[ !( -z "${PATH_FILES_OVERRIDE}") ]]; then
    EXTRA_VARS+="files_override=\"${PATH_FILES_OVERRIDE}\" "
fi

EXTRA_VARS+="ws281x_lib=\"${PATH_NEOPIXEL_WS281X_LIB}\" "
EXTRA_VARS+="led_daemon=\"${PATH_LED_DAEMON}\" "
EXTRA_VARS+="build_tv_daemon=\"${PATH_BUILDTV_DAEMON}\" "
EXTRA_VARS+="system_daemon=\"${PATH_SYSTEM_DAEMON}\" "
EXTRA_VARS+="remote_daemon=\"${PATH_REMOTE_DAEMON}\" "
EXTRA_VARS+="notification_client=\"${PATH_NOTIFICATION_CLIENT}\" "


# Build tags
if [[ -z "${1}" ]]; then
    DEPLOY_TAGS+="remove-deploy,"
    DEPLOY_TAGS+="setup-pi,"
    DEPLOY_TAGS+="wallboard,"
    DEPLOY_TAGS+="config,"
    DEPLOY_TAGS+="neopixel-lib,"
    DEPLOY_TAGS+="led-daemon,"
    DEPLOY_TAGS+="build-tv-daemon,"
    DEPLOY_TAGS+="system-daemon,"
    DEPLOY_TAGS+="remote-daemon,"
    DEPLOY_TAGS+="notification-client,"
    DEPLOY_TAGS+="reboot"
else
    DEPLOY_TAGS="${1}"
fi


# Check user wants to continue
echo ""
echo ""
echo "You are about to perform an automated deployment with the following tags:"
echo "${DEPLOY_TAGS}"
echo ""
echo "Running deployment..."


# Run deployment"
ansible-playbook deploy.yml -v -i ${INVENTORY} --extra-vars "${EXTRA_VARS}" --tags "${DEPLOY_TAGS}"
