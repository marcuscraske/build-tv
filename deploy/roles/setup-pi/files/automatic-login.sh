#!/bin/bash

USER=$1

if [[ -z "${USER}" ]]; then
    echo "User not specified"
    exit 1
fi

NEW_STR="1:2345:respawn:/bin/login -f ${USER} tty1 </dev/tty1 >/dev/tty1 2>&1"

# Remove all lines with tty1 i.e. screen
sed -i '/tty1/d' /etc/inittab

# Add our line to automatically login desired user
echo -e "\n\n${NEW_STR}" >> /etc/inittab

echo "Automatic login setup for user: '${USER}'"
