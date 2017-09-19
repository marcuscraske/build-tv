// Hook onload event so that the page can load...
$(window).ready(function(){
    buildTvController.setup();
});

/*
    Build TV controller, used to poll build TV service and render current status of Jenkins.

    The actual job nodes are assigned the time at which they were updated. And then all the jobs are iterated, so
    that if the assigned time at which they were updated is not the latest global value, the job is removed
    (since it was not in the last poll request).
*/
buildTvController = {

    /* The URL of the REST service for retrieving the build statuses of jobs. */
    buildTvServiceUrl: "http://localhost:2700/build-status-daemon/status/get",

    /* The rate at which to poll for changes. */
    pollDelay: 1000,

    /* The data from polling. */
    pollOldData: null,

    /* The time at which a successful polling occurred; assigned to jobs to efficiently remove non-updated items. */
    pollTimestamp: null,


    setup: function()
    {
        // Setup polling
        this.setupPoll();
    },

    setupPoll: function()
    {
        console.info("buildTvController - polling build TV REST service...");

        setInterval(function(){
            try
            {
                $.getJSON(buildTvController.buildTvServiceUrl, function(data) {

                    buildTvController.pollHandle(data);

                }).fail(function(){
                    console.error("buildTvController - failed to retrieve build status from REST service");
                });
            }
            catch (e)
            {
                console.error("buildTvController - failed to poll: " + e);
            }
        }, this.pollDelay);
    },

    pollHandle: function(data)
    {
        // check if data has even changed
        if (this.pollOldData != null && JSON.stringify(data) == JSON.stringify(this.pollOldData))
        {
            console.debug("buildTvController - polled data unchanged");
            return;
        }

        // update poll timestamp
        this.pollTimestamp = dashboardUtils.currentTime();

        // update all the items
        var jobs = data.jobs;
        var job;

        for (var i = 0; i < jobs.length; i++)
        {
            job = jobs[i];
            this.jobUpdate(job.name, job.status);
        }

        // remove items not updated
        this.jobRemoveNonUpdated();

        // sort jobs by status
        var self = this;
        var jobs = $("#jobs").children().sort(function(a, b){
            var statusA = $(a).attr("status");
            var statusB = $(b).attr("status");

            // assign priority
            var priorityA = self.getPriority(statusA);
            var priorityB = self.getPriority(statusB);

            var result = priorityB - priorityA;

            if (result == 0) {
                // sort by name
                result = $(a).text() > $(b).text() ? 1 : -1;
            }

            return result;
        });

        //$("#jobs").detach().appendTo(jobs);
        $("#jobs").children().detach();
        $("#jobs").append(jobs);
    },

    getPriority: function(status)
    {
        switch (status)
        {
            case "BUILD_PROGRESS":
                return 3;
            case "BUILD_FAILURE":
                return 2;
            case "BUILD_UNSTABLE":
                return 1;
            default:
                return 0;
        }
    },

    jobNameEscape: function(name)
    {
        // Escape job name so that spaces and periods become _ to form valid ID
        name = name.replace(/\./g, "_");    // periods
        name = name.replace(/\ /g, "_");    // spaces

        return name;
    },

    jobFetch: function(name)
    {
        return $("#jobs #job_" + name);
    },

    jobUpdate: function(name, status)
    {
        var jobName = this.jobNameEscape(name);
        var element = this.jobFetch(jobName);

        if (element.length == 0)
        {
            // Add the job
            $("#jobs").append("<li id=\"job_" + jobName + "\">" + name + "</li>");

            element = this.jobFetch(jobName);
        }

        // Update the poll time
        $(element).attr("updated", this.pollTimestamp);

        if (status != $(element).attr("status"))
        {
            // Update the class using status
            $(element).removeClass().addClass(status);

            // Update status
            $(element).attr("status", status);

            console.debug("buildTvController - added/updated job: " + $(element).text());
        }
    },

    jobRemoveNonUpdated: function()
    {
        // Iterate each element, check updated attrib matches pollTimestamp -> else remove it
        var self = this;
        $("#jobs li").each(function() {
            if (self.pollTimestamp != $(this).attr("updated"))
            {
                var jobName = $(this).text();
                $(this).remove();
                console.debug("buildTvController - removed job: " + jobName);
            }
        });
    }

};
