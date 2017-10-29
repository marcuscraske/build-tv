# Configuration
All configuration files for the build TV live at `/build-tv/config` on the Pi.

You can manually edit your config, which is useful for getting started. However you should use config on your local
machine, checked into version control, deployed remotely from your machine to build TVs. Refer to
[managing config](../managing-config.md).


## Configuration Files
Each config file is optional:
- [components-enabled.json](components-enabled.md) - enable/disable components of your build TV
- [dashboards.json](dashboards.md) - configure web-pages shown on the screen
- [jenkins.json](jenkins.md) - configure polling Jenkins for build statuses
- [led-patterns.json](led-patterns.md) - configure notifications, turning off the screen etc


### Experimental

#### Build TV API
- [build-tv-api.json](build-tv-api.md) - authenticated API for polling status of build TV
