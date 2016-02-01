#!/usr/bin/env bash

# A simple script to remove the setup files for a host after a deployment.


# -- The host (name) currently being deployed
HOST="$1"

# -- The base files used by build-tv project
FILES_BASE="$2"

# -- The directory of overridden files to be copied to the target
FILES_COMPILATION="${FILES_BASE}/../compile/${HOST}"

echo "Removing compilation files for ${HOST}..."
rm -r ${FILES_COMPILATION}


echo "Complete!"
