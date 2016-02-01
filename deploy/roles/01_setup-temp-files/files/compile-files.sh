#!/usr/bin/env bash

# Script to copy base and override files for a host to a local directory. This used to occur on the remote host,
# but this action is expensive/slow. Instead the setup files are copied, and overwritten, locally to create the final
# compilation of setup files. Once complete, a single directory can be copied to the remote host.


# -- The host (name) currently being deployed
HOST="$1"

# -- The base files used by build-tv project
FILES_BASE="$2"

# -- The base path to the override directory, containing templates for hosts
FILES_OVERRIDE="$3"

# -- The directory of overridden files to be copied to the target
FILES_COMPILATION="${FILES_BASE}/../compile/${HOST}"



echo "Compiling setup files for ${HOST}:"
echo "- output:     ${FILES_COMPILATION}"
echo "- base:       ${FILES_BASE}"
echo "- override:   ${FILES_OVERRIDE}"
echo ""

# Create directory for compiling config for host
echo "Creating compilation directory..."
mkdir -p "${FILES_COMPILATION}"

# Copy base files
echo "Copying base files..."
cp -R "${FILES_BASE}/." "${FILES_COMPILATION}/"

if [[ ! -z "${FILES_OVERRIDE}" ]]; then
    echo "Copying override files..."

    # Read template file, for host, for which templates to copy. The idea is that we do the following:
    # - copy base files
    # - iterate each template name
    # -- copy {files override}\{template name}
    #
    # The idea is to allow multiple inheritence, for overriding/sharing files for an organisation/departments/projects/teams
    TEMPLATE=`cat ${FILES_OVERRIDE}/${HOST}/template`

    while IFS= read -r line; do
        if [[ ! -z "${line}" ]]; then
            echo "- Copying template ${line}..."
            cp -R "${FILES_OVERRIDE}/${line}/." "${FILES_COMPILATION}/"
        fi
    done <<< "${TEMPLATE}"

fi

echo "Complete!"
