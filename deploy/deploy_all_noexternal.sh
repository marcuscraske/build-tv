#!/bin/bash

# Avoids any apt-get commands and time synchronization
./deploy.sh "all" "nopackages=true,notime=true"
