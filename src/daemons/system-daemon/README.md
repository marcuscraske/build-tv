# system-daemon
The purpose of this daemon is to control and inspect the system environment.

## System Metrics
The system daemon can be used to poll for system metrics, which consist of: minimum value, maximum value and a value; for a type of measurable resource. The following metrics are currently supported:

- CPU
- RAM
- Temperature

## Screen
The physical screen attached to the system can be controlled. At present this only supports the Raspberry Pi 3,
using the `tv-service` to control the HDMI port.

## Power Management
The system can be rebooted or halted.
