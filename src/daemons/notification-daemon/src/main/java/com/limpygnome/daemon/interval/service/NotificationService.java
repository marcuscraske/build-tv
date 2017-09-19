package com.limpygnome.daemon.interval.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.rest.RestRequest;
import com.limpygnome.daemon.common.rest.RestResponse;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.api.Notification;

import com.limpygnome.daemon.interval.notification.NotificationSource;
import com.limpygnome.daemon.util.EnvironmentUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * A service for providing notificationSources to a notification client.
 */
public class NotificationService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(NotificationService.class);

    public static final String SERVICE_NAME = "notifications";

    private static final String LOCAL_SOURCE_NAME = "notifications-service";

    /* Key is the source, value is the notification. */
    private Map<String, NotificationSource> notificationSources;


    public NotificationService()
    {
        this.notificationSources = new HashMap<>();
    }

    @Override
    public void start(Controller controller)
    {
        // Set default startup notification to be hostname
        String ip = EnvironmentUtil.getIpAddress();
        String hostname = EnvironmentUtil.getHostname();

        if (ip == null)
        {
            ip = "unknown";
        }

        Notification notification = new Notification(
            hostname, "ip: " + ip, 30000, "startup", 0
        );

        updateCurrentNotification(LOCAL_SOURCE_NAME, notification);
    }

    @Override
    public void stop(Controller controller)
    {
        // Wipe notificationSources
        notificationSources.clear();
    }

    public synchronized void removeNotificationSource(String source)
    {
        notificationSources.remove(source);
        LOG.info("Removed notification - source: {}", source);
    }

    public synchronized void updateCurrentNotification(String source, Notification notification)
    {
        // Check if we locally have another notification
        NotificationSource notificationSource = notificationSources.get(source);

        if (notificationSource != null)
        {
            // Attempt to update the current notification for the source
            notificationSource.setNotification(notification);

            LOG.debug("Notification source updated - source: {}, notification: {}", source, notification);
        }
        else
        {
            // Create new local source
            notificationSource = new NotificationSource(source, notification);
            notificationSources.put(notificationSource.getSource(), notificationSource);

            LOG.debug("Notification source created - source: {}, notification: {}", source, notification);
        }
    }

    public synchronized NotificationSource getHighestNotificationSource()
    {
        // Find notification with highest priority
        NotificationSource highest = null;
        NotificationSource notificationSource;

        Iterator<Map.Entry<String, NotificationSource>> iterator = notificationSources.entrySet().iterator();
        Map.Entry<String, NotificationSource> kv;

        while (iterator.hasNext())
        {
            kv = iterator.next();
            notificationSource = kv.getValue();

            // Check if notification has expired
            if (notificationSource.isExpired())
            {
                LOG.debug("Notification source has expired, removing... - {}", notificationSource);

                // Remove the key-value pair, no longer needed...
                iterator.remove();
            }
            else if (highest == null || notificationSource.compareTo(highest) >= 0)
            {
                highest = notificationSource;
            }
        }

        LOG.debug("Highest notification source - : {}", highest);

        return highest;
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        // Check request destined for us
        if (restRequest.isPathMatch(new String[]{ "notification-daemon", "notifications", "get" }))
        {
            return handleNotificationGet(restRequest, restResponse);
        }
        else if (restRequest.isJsonRequest() && restRequest.isPathMatch(new String[] { "notification-daemon", "notifications", "set" }))
        {
            return handleNotificationSet(restRequest, restResponse);
        }
        else if (restRequest.isJsonRequest() && restRequest.isPathMatch(new String[]{ "notification-daemon", "notifications", "remove" }))
        {
            return handleNotificationRemove(restRequest, restResponse);
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

            // Build into response message
            JSONObject response = new JSONObject();

            response.put("timestamp", notificationSource.getLastUpdated());
            response.put("header", notification.getHeader());
            response.put("text", notification.getText());
            response.put("lifespan", notification.getLifespan());
            response.put("type", notification.getType());

            // Write response
            restResponse.writeJsonResponseIgnoreExceptions(response);
        }
        else
        {
            // No object available, just provide empty json object...
            restResponse.writeResponseIgnoreExceptions(restResponse, "{}");

            LOG.debug("REST response - no new notification available");
        }

        return true;
    }

    public boolean handleNotificationSet(RestRequest restRequest, RestResponse restResponse)
    {
        JSONObject request = restRequest.getJsonRoot();

        // Read required params
        // -- Notification
        String header = (String) request.get("header");
        String text = (String) request.get("text");
        long lifespan = (long) request.get("lifespan");
        String type = (String) request.get("type");

        // -- Source
        String source = (String) request.get("source");
        int priority = (int) (long) request.get("priority");

        // Build notification
        Notification notification = new Notification(header, text, lifespan, type, priority);

        // Update current notification
        updateCurrentNotification(source, notification);

        return true;
    }

    public boolean handleNotificationRemove(RestRequest restRequest, RestResponse restResponse)
    {
        JSONObject request = restRequest.getJsonRoot();

        String source = (String) request.get("source");

        if (source != null)
        {
            removeNotificationSource(source);
            return true;
        }

        throw new IllegalArgumentException("Missing source from notification removal request");
    }

}
