# Build Indicator
This project currently supports NeoPixel ws281x LED strips.

A useful resource on wiring:

<https://learn.adafruit.com/neopixels-on-raspberry-pi/wiring>

The approach tested on many devices is using a diode, as this is very cheap. Use the guide above, but in summary;
- Connect `din` (data in) from LED strip to GPIO pin on Raspberry Pi (pin 18 by default).
    - <https://www.raspberrypi.org/documentation/usage/gpio/>
- Connect `5v` from LED strip to a diode, and then the diode to a 5v source.
- Connect `gnd` to ground source.

Recommendations:
- Do not use the `5v` from Raspberry Pi, as the power consumption could damage the board.
- Ground should be connected to both the Raspberry Pi's ground and power source's ground.
