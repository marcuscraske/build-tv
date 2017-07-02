# Getting Started
By the end of this guide, you should have a working build TV.


## Prerequisites
- Raspberry Pi 2+
- MicroSD with Raspbian
    - You will need a MicroSD or SD card slot (with MicroSD converter) on your machine
    - See next section for installation
- TV / monitor with HDMI port and cable to connect Raspberry Pi
- Power supply (USB 5v / 2 amps)
    - Optional if you're able to power the Raspberry Pi off the TV
    - When the Pi does not have enough power, you will observe a coloured wheel on the screen
- Network connection
- Spare keyboard for installation process.

If you want a build indicator LED strip, refer to [build indicator](build-indicator.md), this can be added later.


## Installing Raspbian
Grab Raspbian:
<https://www.raspberrypi.org/downloads/raspbian> - Jesse Lite recommended

You can install the image using the official guide:
<https://www.raspberrypi.org/documentation/installation/installing-images>

Or for Linux users, use our quick script:

- Extract the downloaded image and copy the image to `scripts/images` (from root of repository).
- Run `sudo scripts/img-install.sh` to install image.


## Initial Setup
- Install Raspbian onto MicroSD, insert into Pi.
- Add keyboard to Pi, this will just be for the initial installation.
- Connect your Raspberry Pi to your TV (and power source).
- Login as user "pi", password "raspberry".
- Type `ifconfig` to get the IP address of your Pi.
    - For those using a Raspberry Pi 3 with wi-fi, setup your wi-fi connection
    - <https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md>
- Enable SSH daemon using `sudo raspi-config`, go to `interfacing options` and select `SSH`.
- Run `deploy/initial-setup.sh` and follow the instructions.
    - Refer to [manual software setup](manual-software-setup.md) for other platforms, or if the script fails.

## Configuration
The next step is [configuration](configuration.md).
