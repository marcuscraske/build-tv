# Managing Config
This is the recommended process, as you can check-in your build TV's configuration into version control. Useful for any
quick re-setup and managing lots of build TVs.

Configuration lives in `../build-tv-config` (relative to root of this repository), which has a directory `environments`.
Under `environments` is a directory for each build TV.

Example:
`/build-tv-config/environments/team-foobar`

Under each build TV directory, you can create the following optional files/directories:
- `config` (directory) - copied to `/build-tv/config`
- `website` (directory) - copied to `/build-tv/website`, root folder of reusable HTML files and assets for dashboards.
- `system` (directory)
    - `apt-get-proxy-settings` (file) - proxy settings for apt-get, copied to `/etc/apt/apt.conf.d/10proxy`
    - `environment` (file) - copied to `/etc/environment`, useful for proxy settings
    - `ssh_keys` (file) - copied to `authorized_keys` file for `monitor` user.
- `boot-splash-screen` (directory)
    - `splash` (file) - boot screen image, shown when the build TV first boots up

Navigate to the `deploy` directory in this repository.
