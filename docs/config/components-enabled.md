# Components Enabled
Chooses which components are enabled/disabled.

In most cases you will never want to touch this, unless you have an LED strip.


| Key               | Description |
| ----------------- | ----------- |
| remote-daemon     | Opens up build TV API (experimental). |
| led-daemon        | Runs daemon for operating LED strip. See [build indicator](../build-indicator.md). |
| build-tv-daemon   | Polls instance(s) of Jenkins for build statuses. |
| interval-daemon   | Looks at led-patterns to show screen notifications and turn on/off screen. |
| system-daemon     | Provides system metrics, power management and screen APIs.
| launcher-client   | Desktop client to control browser (Chrome) to render dashboards. |
