#!/bin/bash

# Remove existing setting
sed -i '/UseDNS/d' /etc/ssh/sshd_config

# (Re-)add setting
echo -e "\nUseDNS no" >> /etc/ssh/sshd_config

# Reload SSH service
service ssh reload
