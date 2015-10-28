#remote-daemon
The remote daemon is intended to act as an authentication firewall, whereby other daemons will only listen on the local interface. The remote daemon is intended to listen on all interfaces, but only forward requests which have a valid authentication token, thus preventing tampering from remote machines...or other developers. This daemon will, if enabled, forward statistics and the auth token to a specified REST endpoint.

## Authentication
The present implementation generates a 256 alpha-numeric-character token on startup, which is only stored in memory and used for the duration of when the daemon is running.

## Request Forwarding
Requests are forwarded to other daemons based upon the first directory in the relative URL of the request. This mapping of directory name to daemon is maintained by the enum `DaemonType`.

## Stats Forwarding
Stats forwarding will periodically poll its self and other daemons for the following information:

- System metrics - CPU, memory usage, etc.
- Jira dashboard - the current wallboard/dashboard displayed

This information is forwarded to a configurable REST endpoint, found in the file `.settings.remote-daemon.json`, in the relative directory `../deploy/files/config`.
