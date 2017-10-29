# LED Daemon
The purpose of this daemon is to control physical LEDs, with segregation from other daemons allowing this daemon to
load system libraries and unexpectedly fail independently, without taking down the entire TV.

## NeoPixel ws281x Support
This daemon by default supports only the ws281x LED strip, configured for 60 LEDs. Configuration can be found
at `../deploy/config`, in the file `.settings.led-daemon.json`.

## Supporting Other Hardware
You should be able to add support for additional hardware by implementing `LedController` and switching the
implementation used by `LedService.start`.
