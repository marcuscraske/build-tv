#!/bin/bash

# Set timezone to UK
echo "Europe/London" > /etc/timezone

# Reconfigure system timezone
dpkg-reconfigure -f noninteractive tzdata
