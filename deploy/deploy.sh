#!/usr/bin/env bash

# Fancy script title
IFS='' read -r -d '' SCRIPT_HEADER <<'EOF'
 _           _ _     _   _                    _
| |__  _   _(_) | __| | | |___   __  ___  ___| |_ _   _ _ __
| '_ \| | | | | |/ _` | | __\ \ / / / __|/ _ \ __| | | | '_ \
| |_) | |_| | | | (_| | | |_ \ V /  \__ \  __/ |_| |_| | |_) |
|_.__/ \__,_|_|_|\__,_|  \__| \_/   |___/\___|\__|\__,_| .__/
                                                       |_|
EOF

echo -e "\033[1;31m
${SCRIPT_HEADER}
\033[1;32m
"

# Target hosts group
TARGET_HOSTS="build-tv"

# Build dynamic paths
PATH_CURR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PATH_BASE=$(dirname "${PATH_CURR}")

PATH_FILES_BASE="${PATH_CURR}/files"
PATH_FILES_OVERRIDE="${PATH_CURR}/../../build-tv-config/environments"

PATH_NEOPIXEL_WS281X_LIB="${PATH_BASE}/src/libs/neopixel-ws281x-lib"
PATH_LED_DAEMON="${PATH_BASE}/src/daemons/led-daemon"
PATH_BUILD_STATUS_DAEMON="${PATH_BASE}/src/daemons/build-status-daemon"
PATH_NOTIFICATION_DAEMON="${PATH_BASE}/src/daemons/notification-daemon"
PATH_SYSTEM_DAEMON="${PATH_BASE}/src/daemons/system-daemon"
PATH_DASHBOARD_DAEMON="${PATH_BASE}/src/daemons/dashboard-daemon"
PATH_REMOTE_DAEMON="${PATH_BASE}/src/daemons/remote-daemon"
PATH_LAUNCHER_CLIENT="${PATH_BASE}/src/clients/launcher-client"

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
echo "Path - build status daemon:   ${PATH_BUILD_STATUS_DAEMON}"
echo "Path - notification daemon:   ${PATH_NOTIFICATION_DAEMON}"
echo "Path - system daemon:         ${PATH_SYSTEM_DAEMON}"
echo "Path - dashboard daemon:      ${PATH_DASHBOARD_DAEMON}"
echo "Path - remote daemon:         ${PATH_REMOTE_DAEMON}"
echo "Path - launcher client:       ${PATH_LAUNCHER_CLIENT}"


# Build extra vars
# -- Path to base of all files
EXTRA_VARS+="files_base=\"${PATH_FILES_BASE}\" "

# -- Path to file overrides
if [[ !( -z "${PATH_FILES_OVERRIDE}") ]]; then
    EXTRA_VARS+="files_override=\"${PATH_FILES_OVERRIDE}\" "
fi

# -- Paths to daemons/apps etc
EXTRA_VARS+="ws281x_lib=\"${PATH_NEOPIXEL_WS281X_LIB}\" "
EXTRA_VARS+="led_daemon=\"${PATH_LED_DAEMON}\" "
EXTRA_VARS+="build_status_daemon=\"${PATH_BUILD_STATUS_DAEMON}\" "
EXTRA_VARS+="notification_daemon=\"${PATH_NOTIFICATION_DAEMON}\" "
EXTRA_VARS+="system_daemon=\"${PATH_SYSTEM_DAEMON}\" "
EXTRA_VARS+="dashboard_daemon=\"${PATH_DASHBOARD_DAEMON}\" "
EXTRA_VARS+="remote_daemon=\"${PATH_REMOTE_DAEMON}\" "
EXTRA_VARS+="launcher_client=\"${PATH_LAUNCHER_CLIENT}\" "

# Attach any args after first as variables
EXTRA_VARS+="${@:2} "

# Check tags param passed
if [[ -z "${1}" ]]; then
    # Reset terminal colour
    echo -e "\033[0m"

    # Output documentation
    echo "Expected format: deploy.sh <type> <variables/flags added to Ansible... >"
    echo " "
    echo "Available types:"
    echo "- all         : full deployment of all tags"
    echo "- backup      : creates a backup of the current installation"
    echo "- setup       : environmental setup of the host"
    echo "- config      : deploys config"
    echo "- apps        : deploys apps"
    echo "- restart     : restarts daemons"
    echo "- reboot      : reboot hosts"
    echo " "
    echo "Available boolean variables:"
    echo "- nopackages  : disable interaction with package manager"
    echo "- notime      : disable synchronising time"
    echo "- noproxy     : disable adding proxy configuration"
    echo ""
    echo "Example usage of boolean variable:"
    echo "./deploy all \"nopackages=true,notime=true,noproxy=true\""
    echo ""

    exit 1
fi

# Replace alias tags
DEPLOY_TAGS="${1}"

# Reset terminal colour
echo -e "\033[0m"

# Check user wants to continue
echo ""
echo ""
echo "You are about to perform an automated deployment with the following tags:"
echo "${DEPLOY_TAGS}"
echo ""
echo "And the following variables:"
echo "${EXTRA_VARS}"
echo ""
echo "Running deployment..."

# Include Ansible on path
source ${PATH_CURR}/ansible/hacking/env-setup -q

# Run deployment"
ansible-playbook ${PATH_CURR}/deploy.yml -v -i "${INVENTORY}" -l "${TARGET_HOSTS}" --extra-vars "${EXTRA_VARS}" --tags "${DEPLOY_TAGS}"
