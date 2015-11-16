# launcher-client
The launcher client is used to control the dashboard, which runs within a non-headless windowed environment. It also provides a public URL of the dashboard currently displayed.

## Configuration

### Dashboard Provider
Multiple dashboard providers are available, which can produce the URL used to display a dashboard, as well as a public URL to be shared externally.

The provider, as well as its parameters, can be changed in th ``dashboard.json`` file through the following;

```
{
    "provider" : "provider name here"
}
```

#### default
Used to display a simple URL. It should be noted that the public URL is optional, which will inherit the ``url`` setting if not provided.

```
{
    "provider"      : "default",
    "params"        :
    {
        "url"           : "http://www.example.com/?user=secret&pass=secret",
        "public.url"    : "http://www.example.com"
    }
}
```

#### jira
Used to display a Jira dashboard, which uses the Atlassian Wallboard plugin to display a dashboard - this plugin must be installed on your Jira instance.

```
{
    "provider"      : "jira",
    "params"        :
    {
        "url"       : "http://base.path.of.jira.com",
        "user"      : "jira username",
        "pass"      : "jra password",
        "dashboard" : "id of dashboard e.g. 100123"
    }
}
```

### Refresh Time
The dashboard is refreshed periodically, which can be controlled through the ``dashboard.json`` file. This configuration is optional and defaults to 00:00 every day.

```
{
    ...
    "refresh" :
    {
        "hour"      : "14",
        "minute"    : "30"
    }
}
```
