# Raspberry Pi Miscellaneous Repository
This repository hosts various daemons and utils used for a build TV powered by a Raspberry Pi. Each repository has its own README with more detail.

## Modules
List of modules:
- **deploy** - used for automated deployment of most projects for a build TV.
- **daemon-lib** - shared library between daemons
- **neopixel-ws281x-lib** - used to compile a third-party library to control the physical LED hardware, and build a JNI interface.
- **build-tv-daemon** - used to poll Jenkins instances for build status and set LED pattern.
- **led-daemon** - used to interface with the underlying LED hardware library using JNI.
- **screen-daemon** - used to control the screen connected to the machine on which the daemon is running.
- **scripts** - useful scripts

The daemons interact through a REST endpoint, which other external services can use. This also allows
services to be distributed across multiple hosts.

## To-do...
Upcoming features:
- More efficient polling for a cluster of build TVs, perhaps through either multicasting or master/slave architecture.
- REST service authentication.
- LED pattern source moved away from build TV and implemented in the LED daemon, so multiple daemons can set the LED pattern with different priorities.
- Improved standup LED pattern (more colourful e.g. rainbow).
- Voice recognition and control using Google Speech API (if even possible / security risks); refer to JARVIS project.
