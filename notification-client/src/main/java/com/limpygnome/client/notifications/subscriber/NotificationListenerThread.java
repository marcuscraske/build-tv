package com.limpygnome.client.notifications.subscriber;

import com.limpygnome.client.notifications.ui.MessageWindow;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * Created by limpygnome on 27/08/15.
 */
public class NotificationListenerThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(NotificationListenerThread.class);

    private MessageWindow messageWindow;
    private String notificationsEndpoint;
    private long currentTimestamp;

    public NotificationListenerThread(String notificationsEndpoint)
    {
        this.messageWindow = null;
        this.notificationsEndpoint = notificationsEndpoint;
        this.currentTimestamp = 0;
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

            // Wait a while before continuing...
            if (!isExit())
            {
                try
                {
                    Thread.sleep(2000);
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

                messageWindow = new MessageWindow(
                        header, text, lifespan, backgroundR, backgroundG, backgroundB
                );

                LOG.info("Created new window - header: {}, text: {}", header, text);
            }
        }
    }

    public synchronized void closeCurrentWindow()
    {
        if (messageWindow != null)
        {
            messageWindow.close();
            LOG.info("Closed existing window");
        }
    }

}
