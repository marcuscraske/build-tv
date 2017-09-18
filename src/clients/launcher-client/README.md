# launcher-client
Responsible for launching the dashboard within a supported browser (currently Chromium), with process recovery.

This will run an instance of a web server (Jetty) on `localhost`, which will host any web files, required to get around
browser security policies.
