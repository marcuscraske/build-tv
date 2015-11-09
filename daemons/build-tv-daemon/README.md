# build-tv-daemon
## Overview
The build-tv-daemon is used to periodically poll Jenkins for the status of jobs, as well as provide notifications to the notifications-client.

## Endpoints

| Relative Path                             | Purpose                                                                   |
| ----------------------------------------- | ------------------------------------------------------------------------- |
| /build-tv-daemon/notifications/get        | Retrieves the current notification with the highest priority.             |
| /build-tv-daemon/notifications/set        | Set a notification from a source.                                         |
| /build-tv-daemon/notifications/remove     | Removes a notification by source name.                                    |

## Notifications
There can be multiple notifications present at any time in the service, but the actual current notification is based on the notification with the highest priority. Each individual notification must have a unique source.

## Jenkins
The following features are supported:

- Polling multiple instances of Jenkins.
- Polling all jobs on a pipeline/section of a Jenkins instance.
- Polling a specific list of jobs on a Jenkins instance.
