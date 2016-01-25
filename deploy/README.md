# deploy
The purpose of this module is to allow the automated environment setup and deployment of daemons to Raspberry Pi instances.

## Requirements
You will require the following:

- Ansible 1.9.2 (tested/supported)

## Running a Deployment
You can use the ``install`` Maven lifecycle goal of this module to run a deployment:

````
mvn clean install
````

## Overriding Configuration
You can override the default configuration, which either adds or copies over pre-existing files. The path of the overriding configuration is controlled by the variable `PATH_FILES_OVERRIDE` in `deploy.sh` - by default this looks at the path `../../build-tv-config` (relative to this module's path).

The purpose of overriding configuration is to have private files in a separate repository.
