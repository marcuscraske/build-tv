#!/bin/bash

USER=$1

if [[ -z "${USER}" ]]; then
    echo "User not specified"
    exit 1
fi

# replace lines starting with ExecStart with desired config
EXEC_START="ExecStart=-/sbin/agetty --noclear --autologin wallboard %I $TERM"

sudo sed -ri "s;ExecStart=.*;${EXEC_START};g" /lib/systemd/system/getty@.service
