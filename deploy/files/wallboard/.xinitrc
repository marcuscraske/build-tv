#!/bin/sh

# Disable screensaver
xset s off

# Turn off DPMS (power saving mode)
xset -dpms

# Prevent screen from blanking
xset s noblank

# Launch basic window manager
matchbox-window-manager -use_titlebar no -use_cursor no &

# Start notification client
(cd /rpi-misc; java -jar /rpi-misc/notification-client.jar) &

# Start launcher client
(cd /rpi-misc; java -jar /rpi-misc/launcher-client.jar)
