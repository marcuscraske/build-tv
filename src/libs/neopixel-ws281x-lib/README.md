# neopixel-ws281x-lib
This module contains raw C source code and compiles the NeoPixel ws281x underlying shared library/binary. It also produces the Java Native Interface (JNI) interfaces, which are placed into the `src` directory after a clean build.

## Building
You can use the `install` lifecycle goal of Maven for this module to build the binary and JNI interfaces:

````
mvn clean install
````

## Credit
This module contains help and code from the following repositories:

- Original library for Raspberry Pi: https://github.com/jgarff/rpi_ws281x
- Changes to support Raspberry Pi 2: https://github.com/richardghirst/rpi_ws281x
