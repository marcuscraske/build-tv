## Architecture
It's intended that all the daemons will be running on a dedicated box, such as a Raspberry Pi. But all the daemons,
except remote-daemon, only listen on the local interface. Remote-daemon is then responsible for authenticating remote
requests, which are then forwarded to the relevant local daemon based on the first directory in the requested URL.
Local daemons can communicate between each other, with all communication using a form of REST, or rather HTTP POST and
JSON.

The notification client is intended to be executed within a windowed/UI/non-headless environment, which communicates
with the build-status-daemon for notifications to be displayed on the physical screen connected to the box.

Systems other than the Raspberry Pi 2 can be supported, refer to each daemon individually for any notes regarding
compatibility. If not stated, it should be assumed cross-platform support is available for that specific application
or/and library.
