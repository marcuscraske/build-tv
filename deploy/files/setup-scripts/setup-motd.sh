#!/bin/bash

# Remove default MOTD
rm /etc/motd

# Remove last login from sshd
sed -i '/PrintLastLog/d' /etc/ssh/sshd_config

# Restart sshd service
service ssh restart
