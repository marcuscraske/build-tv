# Raspberry Pi Build TV
This repository hosts various service daemons, applications and utilities for a build TV, powered by a Raspberry Pi 2.


## Modules
Each module has its own README with more detail.

- **deploy** - used for automated deployment of most projects for a build TV.
- **notification-client** - used to retrieve, manage and display notifications on a screen.

### Daemons
- **build-tv-daemon** - used to poll Jenkins instances for build status and set LED pattern.
- **led-daemon** - used to interface with the underlying LED hardware library using JNI to invoke low-level functions in a compiled binary library.
- **remote-daemon** - used as the only non-locally exposed endpoint to forward/proxy requests to other (local) daemons with authentication.
- **system-daemon** - used to control the system/environment, such as the screen connected to the machine and power management.

### Libraries
- **daemon-lib** - shared library between daemons.
- **neopixel-ws281x-lib** - used to compile a third-party library binary to control the physical LED hardware, and build a JNI interface.


## Architecture
It's intended that all the daemons will be running on a dedicated box, such as a Raspberry Pi. But all the daemons, except remote-daemon, only listen on the local interface. Remote-daemon is then responsible for authenticating remote requests, which are then forwarded to the relevant local daemon based on the first directory in the requested URL. Local daemons can communicate between each other, with all communication using a form of REST, or rather HTTP POST and JSON.

The notification client is intended to be executed within a windowed/UI/non-headless environment, which communicates with the build-tv-daemon for notifications to be displayed on the physical screen connected to the box.

Systems other than the Raspberry Pi 2 can be supported, refer to each daemon individually for any notes regarding compatibility. If not stated, it should be assumed cross-platform support is available for that specific application or/and library.


## Setup a New Raspberry Pi
1.  Install Debian Wheezy onto SD card.
2.  Boot-up Pi, expand file-system, enable SSH, set hostname.
3.  Edit the ``hosts_inventory`` file located in the ``deploy`` directory. Change ``pi2-team-example`` to the hostname, where referenced, and change ``ansible_ssh_user`` to ``pi`` (or default sudo user available with SSH for Pi).
4.  Run `deploy_full_noproxy.sh` or ``deploy.sh`` for a full installation.
5.  The Pi should reboot as apart of the deploy process, setup complete!

Although the above process should be that simple, don't hesitate to contact me or raise an issue if you encounter any problems. This process should work for any version of the Raspberry Pi, however the LED strip drivers are only compatible with the Raspberry Pi 2. However these drivers can be downgraded for backwards support.


## To-do...
- Improve documentation
- Explore more efficient polling for a cluster of build TVs, perhaps through either multicasting or master/slave architecture.
- Explore voice recognition and control using Google Speech API (if even possible / security risks); refer to JARVIS project.
