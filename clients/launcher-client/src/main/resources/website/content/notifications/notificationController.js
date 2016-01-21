// Hook onload event so that the page can load...
$(window).ready(function(){
    notificationController.setup();
});

// Notification controller, for displaying notifications from the notification service
notificationController = {

    /* The rate at which to poll for dashboard changes in milliseconds. */
    notificationPollingInterval: 1000,

    /* The current notification; set to null once expired. */
    currentNotification: null,

    /* The actual notification element. */
    notificationElement: null,

    /* The notification header element for main description/text.. */
    notificationElementHeader: null,

    /* The notification text element for description/text. */
    notificationElementText: null,

    setup: function()
    {
        // Fetch notification element
        this.notificationElement = $("#notification")[0];
        this.notificationElementHeader = $("#notification .header")[0];
        this.notificationElementText = $("#notification .text")[0];

        // Setup periodic calls
        setInterval(this.pollNotifications, this.notificationPollingInterval);

        console.info("notificationController - setup complete");
    },

    pollNotifications: function()
    {
        console.info("notificationController - polling notification REST service...");

        $.getJSON("http://localhost:2800/interval-daemon/notifications/get", function(data){

            notificationController.handlePollData(data);

        }).fail(function(){
            console.error("dashboardController - failed to retrieve dashboards from REST service");
        });
    },

    handlePollData: function(notification)
    {
        if (notification != null && notification.timestamp != null)
        {
            var currentTimestamp = dashboardUtils.currentTime();

            // Check if notification has changed
            if (this.currentNotification == null || this.currentNotification.timestamp != notification.timestamp)
            {
                // Check actual message received
                if (notification.header == null && notification.text == null)
                {
                    console.info("notificationController - notification set to null");

                    // No notification available...
                    this.hide();
                }
                else
                {
                    console.info("notificationController - new notification received...");

                    // New notification...
                    this.show(notification);
                }
            }
            // Check if notification has expired
            else if (this.currentNotification.lifespan != 0 && currentTimestamp - this.currentNotification.lifespan >= this.currentNotification.timestamp)
            {
                console.info("notificationController - current notification has expired");

                this.hide();
            }
        }
        else
        {
            console.debug("notificationController - no notification object or timestamp received, no notification set");

            // Invalid response received...
            this.hide();
        }
    },

    show: function(notification)
    {
        console.debug("notificationController - showing notification...");

        // Update current notification and timestamp
        this.currentNotification = notification;

        // Update notification element
        $(this.notificationElementHeader).text(notification.header);
        $(this.notificationElementText).text(notification.text);

        // Show notification element
        // -- We add the notification type as a class
        $(this.notificationElement).removeClass().addClass("show").addClass(notification.type);
    },

    hide: function()
    {
        console.debug("notificationController - hiding notification...");

        // Reset current notification
        this.currentNotification = null;

        // Hide notification element
        $(this.notificationElement).removeClass().addClass("hide");
    }

};
