#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Default SSH credentials for raspbian
DEFAULT_USER="pi"
DEFAULT_PASS="raspberry"

# *********************************************************************************
# Sets up Raspberry Pi to be in a state capable of remote configuration.
# *********************************************************************************

# Check if user needs to build source code
echo "Have you built the source code? (y to skip)"
read CONFIRM

if [[ ${CONFIRM} != "y" ]]; then

    # Check Maven installed
    if [[ -x "$(mvn -v)" ]]; then
        echo "ERROR: you need to install Apache Maven!"
        echo ""
        echo "Run ""mvn -v"" to confirm it's installed."
        exit 1
    fi

    # Build source code
    (cd "${DIR}/../src" && mvn clean package)
    if [[ $? != 0 ]]; then
        echo ""
        echo "ERROR: failed to build source code."
        exit 1
    fi

fi


# Install sshpass for later
if [[ -x "$(sshpass)" ]]; then
    sudo apt-get install sshpass
fi


# Install Ansible (if unavailable)
if [[ ! -d "${DIR}/ansible" ]]; then

    echo "Setting up stand-alone copy of Ansible..."

    ## Install pip
    sudo easy_install pip

    ## Fetch Ansible
    git clone git://github.com/ansible/ansible.git --recursive

    ## Checkout stable 2.3
    (cd ansible && git checkout stable-2.3 && git submodule update --init --recursive)

    ## Install dependencies
    sudo pip install -r ${DIR}/ansible/requirements.txt

    ## Check it's working
    source ${DIR}/ansible/hacking/env-setup -q
    ansible --version

    echo ""
    echo "Finished setup of stand-alone Ansible"

else
    echo "Ansible already installed, skipping..."
fi


# Read current user's SSH key
SSH_KEY=`cat ~/.ssh/id_rsa.pub`

if [[ -z "${SSH_KEY}" ]]; then
    echo ""
    echo "ERROR: SSH key not found at ~/.ssh/id_rsa.pub"
    exit 1
fi

# Check SSH key is single line
SSH_KEY_LINES=`echo $SSH_KEY | wc -l`

if [[ ${SSH_KEY_LINES} != 1 ]]; then
    echo ""
    echo "ERROR: SSH key must be only single line (~/.ssh/id_rsa.pub)"
    exit 1
fi


# Ask what the user wants to call this build-tv
echo ""
echo "Desired hostname for build tv? (must have only azAZ-_ chars) e.g. pi-team-foobar"

read BUILD_TV_NAME

if [[ ${BUILD_TV_NAME} = *[^a-zA-Z0-9\_\-]$* ]]; then
    echo "ERROR: inalid build TV name, must be a-zA-Z0-9-_ chars only"
    exit 1
fi

echo ""
echo "What is the IP address of the build TV?"

read BUILD_TV_IP

echo ""
echo "This will setup the build TV using the following config:"
echo "  Host name:  ${BUILD_TV_NAME}"
echo "  IP:         ${BUILD_TV_IP}"
echo ""
echo "Is this correct? (enter y)"

read CONFIRM

if [[ $CONFIRM != "y" ]]; then
    echo "Aborted."
    exit 1
fi


# Run test command to confirm host exists
echo "Attempting test SSH connection to build TV. If this hangs, ctrl+c and check you can ping the IP address..."
sshpass -p${DEFAULT_PASS} ssh -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} -C "whoami"


# Setup build-tv-config directory
## Create base directory with SSH keys, if build-tv-config has never existed
FIRST_TIME="false"

if [[ ! -f "${DIR}/../../build-tv-config/environments/base/system/ssh_keys" ]]; then

    mkdir -p "${DIR}/../../build-tv-config/environments/base/system"
    echo "${SSH_KEY}" >> "${DIR}/../../build-tv-config/environments/base/system/ssh_keys"
    echo "Created base template with SSH key"

    echo -e "[build-tv]\n${BUILD_TV_NAME}\n\n" >> "${DIR}/../../build-tv-config/environments/hosts_inventory"

    FIRST_TIME="true"

else
    echo "Skipped creating base template"
fi


## Create directory for build-tv
if [[ ! -d "${DIR}/../../build-tv-config/environments/${BUILD_TV_NAME}" ]]; then

    mkdir -p "${DIR}/../../build-tv-config/environments/${BUILD_TV_NAME}"

    # Use 'base' template by default; this is to ensure SSH keys are used out of the box
    echo "base" >> "${DIR}/../../build-tv-config/environments/${BUILD_TV_NAME}/template"

    # Append to inventory file
    echo -e "[${BUILD_TV_NAME}]\n${BUILD_TV_NAME}\n\n[${BUILD_TV_NAME}:vars]\nansible_user=monitor\n\n" >> "${DIR}/../../build-tv-config/environments/hosts_inventory"

    echo "Created template for build-tv ""${BUILD_TV_NAME}"""

else
    echo "WARNING: template already exists for build-tv ""${BUILD_TV_NAME}"""
fi

# Setup using default credentials (u: pi, p: raspberry)

## Set hostname
sshpass -p${DEFAULT_PASS} ssh -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} -C "sudo hostnamectl set-hostname ${BUILD_TV_NAME}"
sshpass -p${DEFAULT_PASS} ssh -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} -C "echo ""127.0.1.1 ${BUILD_TV_NAME}"" | sudo tee --append /etc/hosts"
echo "Hostname setup"

## Create monitor user, add SSH key
sshpass -p${DEFAULT_PASS} ssh -t -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} << EOF

    sudo adduser --disabled-password --gecos '' monitor
    sudo usermod -aG sudo monitor
    echo -e 'monitor   ALL=(ALL:ALL) NOPASSWD:ALL' | sudo tee --append /etc/sudoers

EOF
echo "Added monitor user"

sshpass -p${DEFAULT_PASS} ssh -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} -C "sudo mkdir -p /home/monitor/.ssh"
sshpass -p${DEFAULT_PASS} ssh -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} -C "sudo touch /home/monitor/.ssh/authorized_keys"
sshpass -p${DEFAULT_PASS} ssh -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} -C "sudo chown monitor:monitor /home/monitor/.ssh/authorized_keys"
sshpass -p${DEFAULT_PASS} ssh -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} -C "sudo chmod 700 /home/monitor/.ssh/authorized_keys"
sshpass -p${DEFAULT_PASS} ssh -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} -C "echo ${SSH_KEY} | sudo tee --append /home/monitor/.ssh/authorized_keys"
echo "Addd SSH key to monitor user"

sshpass -p${DEFAULT_PASS} ssh -t -o StrictHostKeyChecking=no ${DEFAULT_USER}@${BUILD_TV_IP} << EOF
    sudo reboot
EOF

echo ""
echo "Build TV setup!"

echo ""
echo ""
echo "***************************************************************"
echo "Instructions"
echo "***************************************************************"
echo "If this is not your first build-tv, you will need to edit the following file:"
echo "    ${DIR}/../../build-tv-config/environments/hosts_inventory"
echo ""
echo "And then add ""${BUILD_TV_NAME}"" under the ""build-tv"" section."
echo ""
echo ""
echo "If you are on a network behind a proxy, please read the following in the docs:"
echo "    docs/configure-proxy.md"
echo ""
echo "Once ready and your Pi has finished rebooting, you can continue the process to fully setup your build TV."
echo ""
echo "Run the following for non-proxy environment:"
echo "    ${DIR}/deploy_all_noproxy.sh"
echo ""
echo "Or otherwise the following for when on a network behind a proxy:"
echo "    ${DIR}/deploy.sh all"
echo ""
