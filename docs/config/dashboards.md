# Dashboards
Display multiple dashboards, with their own timings and refreshed at defined intervals in the background.

Define each dashboard as a JSON object inside the following:

````
{
    "pages" :
    [
        <place dashboards here>
    ]
}
````

## Dashboards
The following types of dashboard providers are available:
- `jira` - display a Jira wallboard
- `url` - display a URL; can be external or a deployed HTML file

The following generic settings are available for all dashboards:
- `lifespan` - the length of time (milliseconds) the dashboard is displayed
- `refresh` - the interval (milliseconds) at which the dashboard is refreshed. Dashboards only refresh when not shown, unless there's only a single dashboard.

### JIRA Dashboard
Display a wallboard from JIRA.

You will need the ID of the wallboard, which can be found by hovering over any links to a wallboard.

**Note:** unfortunately Jira passwords are stored as plain-text right now.

````
{
    "provider" : "jira",
    "params" :
    {
        "url": "content/mock/test_a.html",
        "user": "jira user",
        "pass": "jira pass",
        "dashboard": "wallboard id",
        "lifespan" : 5000,
        "refresh" : 60000
    }
}
````

### URL Dashboard
Display a web page.

**Note:** due to X-Frame issues, a lot of external sites cannot be embedded, as dashboards are displayed through an iframe.

````
{
    "provider" : "url",
    "params" :
    {
        "url": "build-status.html",
        "lifespan" : 20000,
        "refresh" : 0
    }
},
````

The following pages are built-in:
- `build-status.html` - displays the status of every Jenkins job.
- `share-price.html?symbol=GOOG` - displays share price data for a given stock ticker/symbol

#### Custom Pages
You can add custom resources (HTML files, css, etc) under the directory `website` for a host's template in `build-tv-config`.

Lets say you added the asset `mycustompage.html`, the `url` would just be `mycustompage.html`.

## Full Examples

````
{
    "pages" :
    [
        {
            "provider" : "jira",
            "params" :
            {
                "url": "content/mock/test_a.html",
                "user": "user",
                "pass": "pass",
                "dashboard": "test",
                "lifespan" : 5000,
                "refresh" : 60000
            }
        },
        {
            "provider" : "url",
            "params" :
            {
                "url": "build-status.html",
                "lifespan" : 20000,
                "refresh" : 0
            }
        },
        {
            "provider" : "url",
            "params" :
            {
                "url": "share-price.html?symbol=GOOG",
                "lifespan" : 20000,
                "refresh" : 0
            }
        },
        {
            "provider" : "url",
            "params" :
            {
                "url": "http://localhost:2100/?url=http://www.bbc.com",
                "lifespan" : 15000,
                "refresh" : 0
            }
        },
        {
            "provider" : "url",
            "params" :
            {
                "url": "content/mock/test_b.html",
                "lifespan" : 5000,
                "refresh" : 5000
            }
        },
        {
            "provider" : "url",
            "params" :
            {
                "url": "content/mock/test_c.html",
                "lifespan" : 5000,
                "refresh" : 60000
            }
        }
    ]
}

````