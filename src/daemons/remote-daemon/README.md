# remote-daemon
This daemon can be used as an API for a build TV, whereby REST requests can be sent to internal daemons, protected by
a token.

On startup, a token is read from the file `auth-token.txt`, or a 256 alpha-numeric char token is generated and written
to file instead. This token should be sent in all requests:

````
{
    "token" : "put token here",
    ...
}
````
