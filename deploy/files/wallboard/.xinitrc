#!/bin/sh

# Disable screensaver
xset s off

# Turn off DPMS (power saving mode)
xset -dpms

# Prevent screen from blanking
xset s noblank

# Launch basic window manager
matchbox-window-manager -use_titlebar no -use_cursor no &

# Start launcher client
(cd /build-tv; java -jar /build-tv/launcher-client.jar)
