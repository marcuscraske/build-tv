#!/bin/sh

# Load config file
. "/home/wallboard/wallboard.properties"

# Disable screensaver
xset s off

# Turn off DPMS (power saving mode)
xset -dpms

# Prevent screen from blanking
xset s noblank

# Launch basic window manager
matchbox-window-manager -use_titlebar no -use_cursor no &

# Start notification client
#(cd /rpi-misc; java -jar /rpi-misc/notification-client-1.0.jar) &

# Build JIRA url
DISPLAY_URL="${JIRA_URL}/login.jsp?os_username=${JIRA_USERNAME}&os_password=${JIRA_PASSWORD}&os_destination=plugins/servlet/Wallboard/?dashboardId=${JIRA_WALLBOARD_ID}"

# Start Chromium in kiosk mode with Jira wallboard
chromium-browser --kiosk --disable-translate "${DISPLAY_URL}" --window-position=0,0 --window-size=${SCREEN_WIDTH},${SCREEN_HEIGHT}
