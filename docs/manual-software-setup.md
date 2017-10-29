# Manual Software Setup
This section can be skipped using the `deploy/initial-setup.sh` script.


## Configure Pi
This section involves commands you'll need to run on the Pi.

Use Raspberry Config menu to set the desired hostname and enable SSH:

````
sudo raspi-config
````

Now SSH to your Pi, as you'll need to copy your SSH key in the upcoming section.

Add a user called `monitor`, add your SSH key to the `authorized_keys` file:

````
sudo adduser --disabled-password --gecos "" monitor
sudo nano /home/monitor/.ssh/authorized_keys
<paste your SSH key, press ctrl=x, enter y and press enter>
sudo chown monitor:monitor /home/monitor/.ssh/authorized_keys
sudo chmod 700 /home/monitor/.ssh/authorized_keys
````

Reboot your build TV:

````
sudo reboot
````

## Install Ansible
Download a copy of Ansible and place it in the `deploy` directory, under an `ansible` directory.

<https://github.com/ansible/ansible/archive/stable-2.3.zip>

The following file should exist:

````
deploy/ansible/VERSION
````

### Install Python Dependencies
Install `pip` and Ansible's dependencies:

````
sudo easy_install pip
sudo pip install -r ${DIR}/ansible/requirements.txt
````


## Build Config
In the directory above this repository, create a directory called `build-tv-config`. And then add the following
structure:

- `environments` (directory)
- `environments/base/system` (directory)
- `environments/base/system/ssh_keys` (file) - add any SSH keys to be shared across build TVs
- `environments/<hostname of build tv>/template` (file) - put the word "base" in this file.


## Build Software
You will need Maven installed:

<https://maven.apache.org>

Go into the `src` directory from the root of this repository and build the project:

````
mvn clean package
````


## Deploy Software


If your network is not behind a proxy, run:

````
deploy/deploy_all_noproxy.sh
````

Otherwise [configure proxy](configure-proxy.md) and run:

````
deploy/deploy.sh all
````
