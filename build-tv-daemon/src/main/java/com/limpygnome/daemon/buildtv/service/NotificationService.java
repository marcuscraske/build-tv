package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.limpygnome.daemon.api.rest.RestResponse;
import com.limpygnome.daemon.api.rest.RestServiceHandler;
import com.limpygnome.daemon.buildtv.model.Notification;

import com.limpygnome.daemon.buildtv.model.NotificationSource;
import com.limpygnome.daemon.util.EnvironmentUtil;
import java.awt.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * A service for providing notifications to a notification client.
 */
public class NotificationService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(NotificationService.class);

    public static final String SERVICE_NAME = "notifications";

    private static final String LOCAL_SOURCE_NAME = "notifications-service";

    /* Key is the source, value is the notification. */
    private Map<String, NotificationSource> notifications;


    public NotificationService()
    {
        this.notifications = new HashMap<>();
    }

    @Override
    public void start(Controller controller)
    {
        // Set default startup notification to be hostname
        String ip = EnvironmentUtil.getIpAddress();

        if (ip == null)
        {
            ip = "unknown";
        }

        Notification notification = new Notification(
            getHostname(), "ip: " + ip, 10000, Color.DARK_GRAY, 0
        );

        updateCurrentNotification(LOCAL_SOURCE_NAME, notification);
    }

    @Override
    public void stop(Controller controller)
    {
        // Wipe notifications
        notifications.clear();
    }

    public synchronized void updateCurrentNotification(String source, Notification notification)
    {
        // Check if we locally have another notification
        NotificationSource notificationSource = notifications.get(LOCAL_SOURCE_NAME);

        if (notificationSource != null)
        {
            // Attempt to update the current notification for the source
            notificationSource.setNotification(notification);
        }
        else
        {
            // Create new local source
            notificationSource = new NotificationSource(LOCAL_SOURCE_NAME, notification);
            notifications.put(notificationSource.getSource(), notificationSource);

            LOG.debug("Notification source created - source: {}, notification: {}", source, notification);
        }
    }

    public synchronized NotificationSource getHighestNotificationSource()
    {
        // Find notification with highest priority
        NotificationSource highest = null;
        NotificationSource notificationSource;

        Iterator<Map.Entry<String, NotificationSource>> iterator = notifications.entrySet().iterator();
        Map.Entry<String, NotificationSource> kv;

        while (iterator.hasNext())
        {
            kv = iterator.next();
            notificationSource = kv.getValue();

            // Check if notification has expired
            if (notificationSource.isExpired())
            {
                // Remove the key-value pair, no longer needed...
                iterator.remove();
            }
            else if (highest == null || notificationSource.compareTo(highest) >= 0)
            {
                highest = notificationSource;
            }
        }

        return highest;
    }

    private String getHostname()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e)
        {
            return "unknown";
        }
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        // Check request destined for us
        if (restRequest.isPathMatch(new String[]{ "build-tv-daemon", "notifications", "get" }))
        {
            return handleNotificationGet(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[] { "build-tv-daemon", "notifications", "set" }))
        {
            return handleNotificationSet(restRequest, restResponse);
        }
        else
        {
            return false;
        }
    }

    public boolean handleNotificationGet(RestRequest restRequest, RestResponse restResponse)
    {
        // Pull latest notification from service
        NotificationSource notificationSource = getHighestNotificationSource();

        if (notificationSource != null)
        {
            Notification notification = notificationSource.getNotification();
            Color background = notification.getBackground();

            // Build into response message
            JSONObject response = new JSONObject();

            response.put("timestamp", notification.getTimeStamp());
            response.put("header", notification.getHeader());
            response.put("text", notification.getText());
            response.put("lifespan", notification.getLifespan());

            JSONObject resonseBackground = new JSONObject();
            resonseBackground.put("r", background.getRed());
            resonseBackground.put("g", background.getGreen());
            resonseBackground.put("b", background.getBlue());

            response.put("background", resonseBackground);

            // Write response
            restResponse.writeJsonResponseIgnoreExceptions(restResponse, response);
        }
        else
        {
            // No object available, just provide empty json object...
            // TODO: consider if this is correct...
            restResponse.writeResponseIgnoreExceptions(restResponse, "{}");
        }

        return true;
    }

    public boolean handleNotificationSet(RestRequest restRequest, RestResponse restResponse)
    {
        // Read required params

        // Build notification

        // Update current notification

        return true;
    }

}
