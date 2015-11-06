package com.limpygnome.client.notifications.subscriber;

import com.limpygnome.client.notifications.ui.NotificationWindow;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * A thread used to periodically poll an endpoint for notification data and manage notification windows.
 */
public class NotificationListenerThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(NotificationListenerThread.class);

    /* The current notification window. */
    private NotificationWindow notificationWindow;

    /* The URL of the notifications endpoint. */
    private String notificationsEndpoint;

    /* The timestamp of the current notification; used to check if the notification has changed. */
    private long currentTimestamp;

    /*   The ifespan of the current notification. */
    private long currentLifespan;

    public NotificationListenerThread(String notificationsEndpoint)
    {
        this.notificationWindow = null;
        this.notificationsEndpoint = notificationsEndpoint;
        this.currentTimestamp = 0;
        this.currentLifespan = 9;
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName("Listener");

        LOG.info("Listener started...");

        RestClient restClient = new RestClient("Notification Client", -1);
        JSONObject response;

        while(!isExit())
        {
            // Poll endpoint for new message
            try
            {
                response = restClient.executeJson(notificationsEndpoint);
                handleMessage(response);
            }
            catch (Exception e)
            {
                LOG.error("Failed to poll for notification updates", e);
            }

            // Wait a while before re-polling...
            if (!isExit())
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) { }
            }
        }

        closeCurrentWindow();

        LOG.info("Listener finished");
    }

    public synchronized void handleMessage(JSONObject response)
    {
        // Check a message is available
        if (!response.containsKey("timestamp"))
        {
            // Check if current notification has lifespan, if not kill it
            if (notificationWindow != null && currentLifespan == 0)
            {
                closeCurrentWindow();
            }
            return;
        }

        // Check message is different
        long timestamp = (long) response.get("timestamp");

        if (timestamp != currentTimestamp)
        {
            currentTimestamp = timestamp;

            LOG.info("Message changed - curr timestamp: {}, new timestamp: {}", currentTimestamp, timestamp);

            // Close existing message window
            closeCurrentWindow();

            // Check response contains new window
            if (response.containsKey("header") && response.containsKey("text"))
            {
                // Create new window
                String header = (String) response.get("header");
                String text = (String) response.get("text");
                long lifespan = (long) response.get("lifespan");

                JSONObject jsonBackground = (JSONObject) response.get("background");
                int backgroundR = (int) (long) jsonBackground.get("r");
                int backgroundG = (int) (long) jsonBackground.get("g");
                int backgroundB = (int) (long) jsonBackground.get("b");

                notificationWindow = new NotificationWindow(
                        header, text, lifespan, backgroundR, backgroundG, backgroundB
                );

                this.currentLifespan = lifespan;

                LOG.info("Created new window - header: {}, text: {}", header, text);
            }
        }
    }

    public synchronized void closeCurrentWindow()
    {
        if (notificationWindow != null)
        {
            notificationWindow.close();
            notificationWindow = null;

            LOG.info("Closed existing window");
        }
    }

}
