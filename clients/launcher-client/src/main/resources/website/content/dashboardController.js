// Hook onload event so that the page can load...
window.onload = function()
{
    dashboardController.setup();
}

// Our dashboard controller, for creating and changing the visibility of dashboards (which are iframe elements)
dashboardController = {

    /* The index of the current dashboard being shown. */
    currentIframeIndex: -1,

    /* Total number of dashboards. */
    totalDashboards: 0,

    /* The dashboards to be transitioned. */
    dashboards: {},

    retrieveRoot: function()
    {
        return $("#dashboards");
    },

    setup: function()
    {
        // Setup periodic calling of REST service for dashboards...
    },

    resetTransitioning: function()
    {
        var root = this.retrieveRoot();

        // Reset existing dashboards
        $(root).children().removeAll();

        // Add dashboards
        var dashboard;
        var iframe;

        for (var i = 0; i < dashboards.length; i++)
        {
            dashboard = dashboards[i];

            // Create iframe for each dashboard...
            iframe = document.createElement("iframe");
            iframe.id = "dashboard_" + i;
            iframe.src = dashboard.url;
            iframe.lifespan = dashboard.lifespan;

            root.appendChild(iframe);
        }

        this.totalDashboards = dashboards.length;

        console.info("Setup " + this.totalDashboards + " dashboards");

        // Begin transition of dashboaards...
        this.transition();
    },

    retrieve: function(index)
    {
        return $("#dashboard_" + index);
    },

    hide: function(index)
    {
        console.debug("dashboardController - hiding dashboard " + index);

        var iframe = this.retrieve(index);
        iframe.className = "hide";

        // Reload page...
        var currentUrl = iframe.src;
        iframe.src = null;
        iframe.src = currentUrl;
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
            var self = this;
            setTimeout(function() { self.transition(); }, iframe.lifespan);
        }
    }

};
