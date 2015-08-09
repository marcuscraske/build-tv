#!/bin/bash

# Remove existing setting
sed -i '/UseDNS/d' /etc/ssh/shd_config

# (Re-)add setting
echo -e "\nUseDNS no"

# Reload SSH service
service ssh reload
