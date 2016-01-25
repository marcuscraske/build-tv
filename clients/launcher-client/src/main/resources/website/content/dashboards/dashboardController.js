// Hook onload event so that the page can load...
$(window).ready(function(){
    dashboardController.setup();
});

// Our dashboard controller, for creating and changing the visibility of dashboards (which are iframe elements)
dashboardController = {

    dashboardServiceUrl: "http://localhost:2900/dashboards/urls/get",

    /* The index of the current dashboard being shown. */
    currentIframeIndex: -1,

    /* Total number of dashboards. */
    totalDashboards: 0,

    /* The dashboards to be transitioned. */
    dashboards: {},

    /* The rate at which to poll for dashboard changes in milliseconds. */
    dashboardPollingInterval: 1000,

    /* The delay in refreshing a dashboard, to counter CSS transition animations. */
    refreshDelay: 2000,

    retrieveRoot: function()
    {
        return $("#dashboards");
    },

    setup: function()
    {
        // Setup periodic calling of REST service for dashboards...
        setInterval(this.pollDashboards, this.dashboardPollingInterval);

        console.info("dashboardController - setup complete");
    },

    pollDashboards: function()
    {
        console.info("dashboardController - polling dashboard REST service...");

        try
        {
            $.getJSON(dashboardController.dashboardServiceUrl, function(data) {

                // Fetch existing dashboards
                var dashboards = dashboardController.dashboards;
                var newDashboards = data.dashboards;

                // Check if dashboard data has changed...
                if (JSON.stringify(newDashboards) != JSON.stringify(dashboards))
                {
                    // Reset transitioning
                    dashboardController.resetTransitioning(newDashboards);
                }
                else
                {
                    console.debug("dashboardController - polled data unchanged");
                }

            }).fail(function(){
                console.error("dashboardController - failed to retrieve dashboards from REST service");
            });
        }
        catch (e)
        {
            console.error("dashboardController - failed to poll: " + e);
        }
    },

    resetTransitioning: function(data)
    {
        var root = this.retrieveRoot();

        // Update dashboard data
        console.info("dashboardController - dashboard data changed - old: " + this.dashboards + ", new: " + data);

        this.dashboards = data;
        this.currentIframeIndex = -1;

        // Iterate existing dashboards to cancel intervals
        $(root).children().each(function(){
            var iframe = this;

            if (iframe.interval) {
                clearInterval(iframe.interval);
                console.debug("dashboardController - cleared interval attached to iframe");
            }
        });

        // Reset existing dashboards
        $(root).empty();

        // Add dashboards
        var dashboard;
        var iframe;

        for (var i = 0; i < this.dashboards.length; i++)
        {
            dashboard = this.dashboards[i];

            // Create iframe for each dashboard...
            iframe = document.createElement("iframe");
            iframe.id = "dashboard_" + i;
            iframe.src = dashboard.url;
            iframe.lifespan = dashboard.lifespan;
            iframe.refresh = dashboard.refresh;
            iframe.lastRefresh = dashboardUtils.currentTime();
            iframe.className = "hide";

            root.append(iframe);
        }

        this.totalDashboards = this.dashboards.length;

        console.info("dashboardController - setup " + this.totalDashboards + " dashboards");

        // Begin transition of dashboaards...
        this.transition();
    },

    retrieve: function(index)
    {
        return $("#dashboard_" + index)[0];
    },

    hide: function(index)
    {
        console.debug("dashboardController - hiding dashboard " + index);

        var iframe = this.retrieve(index);
        iframe.className = "hide";

        // Hook for refresh if not zero/infinite
        if (iframe.refresh > 0)
        {
            // Reload page if surpassed refresh interval
            var lastRefreshed = iframe.lastRefresh;
            var currentTime = dashboardUtils.currentTime();

            if (currentTime - iframe.refresh >= lastRefreshed)
            {
                // Set delay for refresh incase of any transitioning
                var self = this;

                setTimeout(
                    function()
                    {
                        self.refresh(index, iframe);
                    },
                    this.refreshDelay
                );
            }

            console.debug("dashboardController - hooked refresh for dashboard - index: " + index);
        }
    },

    refresh: function(index, iframe)
    {
        var currentUrl = iframe.src;

        console.debug("refreshing iframe " + index + " - interval: " + iframe.refresh + ", last: " +
                        iframe.lastRefresh + ", url: " + currentUrl
        );

        iframe.src = null;
        iframe.src = currentUrl;
        iframe.lastRefresh = dashboardUtils.currentTime();
    },

    show: function(index)
    {
        console.debug("dashboardController - showing dashboard " + index);

        var iframe = this.retrieve(index);
        iframe.className = "show";
    },

    transition: function()
    {
        var root = this.retrieveRoot();

        // Hide current iframe
        if (this.currentIframeIndex >= 0)
        {
            this.hide(this.currentIframeIndex);
        }

        // Increment to next index
        this.currentIframeIndex++;

        // Check within range
        if (this.currentIframeIndex >= this.totalDashboards)
        {
            this.currentIframeIndex = 0;
        }

        // Display current iframe
        this.show(this.currentIframeIndex);

        // Hook for next dashboard, unless zero i.e. infinite...
        var iframe = this.retrieve(this.currentIframeIndex);

        if (iframe.lifespan > 0)
        {
            // Hook transition for end of lifespan of iframe
            var self = this;
            setTimeout(function() {
                self.transition();
            },
            iframe.lifespan);
        }
        else
        {
            console.debug("dashboardController - infinite/invalid lifespan (" + iframe.lifespan + "), no transitions left...");
        }
    }

};
