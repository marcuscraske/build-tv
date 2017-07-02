# Managing Config
This is the recommended process, as you can check-in your build TV's configuration into version control. Useful for any
quick re-setup and managing lots of build TVs.

## Location
Configuration lives in `../build-tv-config` (relative to root of this repository), which has a directory `environments`.
Under `environments` is a directory for each build TV.

Example:
`/build-tv-config/environments/team-foobar`


## Build TVs
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


## Templating
You can specify a list of directories, so that build TVs can share files/configuration. This works by specifying
a list of directories, which are copied over each other from top to bottom.

In a build TV directory, create the file `template` and specify a folder on each line.

An example for `team-foobar`:

````
organisation
development
````

This would copy the following directories on top of each other:
- `/build-tv-config/environments/organisation`
- `/build-tv-config/environments/development`
- `/build-tv-config/environments/team-foobar`

The final directory at the end would be remotely copied to the build TV. This is useful if you have common splash
screens and dashboard files.

## Deploying
When you want to deploy your configuration changes, run `deploy/deploy_config.sh`.
