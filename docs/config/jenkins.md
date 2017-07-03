# Jenkins
Example to poll all jobs for a Jenkins instance, where the URL is the view of the jobs:

````
"hosts" :
    [
        {
            "name"      : "Apache - Subset",
            "url"       : "https://builds.apache.org/view/All/"
        }
    ]
````

Example to poll specific jobs, from a view, for a Jenkins instance:

````
"hosts" :
    [
        {
            "name"      : "Apache - Subset",
            "url"       : "https://builds.apache.org/view/All/",
            "jobs"      :
            [
                "xmlgraphics-fop",
                "incubator-taverna-server"
            ]
        }
    ]
````

You can poll multiple instances of Jenkins, useful for large environments:

````
"hosts" :
    [

        {
            "name"      : "Apache - Subset",
            "url"       : "https://builds.apache.org/view/All/",
            "jobs"      :
            [
                "xmlgraphics-fop",
                "incubator-taverna-server"
            ]
        },

        {
            "name"      : "Apache - ZooKeeper",
            "url"       : "https://foobar.com/view/All/",
            "jobs"      :
            [
                "foobar-job"
            ]
        }

    ]
````
