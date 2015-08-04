#!/bin/bash

# Disable text terminal blanking
sed "/BLANK_TIME/d"     -i /etc/kbd/config
sed "/POWERDOWN_TIME/d" -i /etc/kbd/config

echo "BLANK_TIME=0"     >> /etc/kbd/config
echo "POWERDOWN_TIME=0" >> /etc/kbd/config

echo "Monitor blanking disabled"
