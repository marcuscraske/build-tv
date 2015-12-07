#!/bin/bash

# Avoids any apt-get commands and time synchronization
./deploy.sh "*" "nopackages=true,notime=true"
