# Components Enabled
Chooses which components are enabled/disabled.

In most cases you will never want to touch this, unless you have an LED strip.


| Key                   | Description |
| --------------------- | ----------- |
| dashboard-daemon      | Runs service to control available dashboards. |
| remote-daemon         | Opens up build TV API (experimental). |
| system-daemon         | Provides system metrics, power management and screen APIs. |
| led-daemon            | Operates LED strip, see [build indicator](../build-indicator.md). |
| build-status-daemon   | Polls instance(s) of Jenkins for build statuses. |
| notification-daemon   | Looks at led-patterns to show screen notifications and turn on/off screen. |
| launcher-client       | Desktop client to control browser (Chrome) to render dashboards. |
