# Jenkins
## Configuration
Configuration is found at:

<pre>
/deploy/files/config/jenkins.json
</pre>

Example to poll all jobs for a Jenkins instance, where the URL is the view of the jobs:

<pre>
"hosts" :
    [
        {
            "name"      : "Apache - Subset",
            "url"       : "https://builds.apache.org/view/All/"
        }
    ]
</pre>

Example to poll specific jobs, from a view, for a Jenkins instance:

<pre>
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
</pre>
