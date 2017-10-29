# Configure Proxy
You will need to configure two files in your `base` template in the `build-tv-config` directory.

Go to the directory above this repository, and then go into `build-tv-config`.

Create the file `environments/base/system/apt-get-proxy-settings`:

````
Acquire::http::Proxy "http://your-proxy:8080";
````

Create the file `environments/base/system/environment`:

````
# Proxy settings  - lower-case
http_proxy="http://your-proxy:8080/"
https_proxy="http://your-proxy:8080/"
ftp_proxy="http://your-proxy:8080/"
socks_proxy="http://your-proxy:8080/"

# Proxy settings - upper-case
HTTP_PROXY="http://your-proxy:8080/"
HTTPS_PROXY="http://your-proxy:8080/"
FTP_PROXY="http://your-proxy:8080/"
SOCKS_PROXY="http://your-proxy:8080/"

no_proxy="127.0.0.1, localhost"

````

And perform a full deployment:

````
deploy/deploy.sh all
````
