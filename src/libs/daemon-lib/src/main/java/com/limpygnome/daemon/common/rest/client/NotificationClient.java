package com.limpygnome.daemon.common.rest.client;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Notification;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.net.ConnectException;

/**
 * REST client used to communicate with notification daemon for notifications.
 */
public class NotificationClient
{
    private static final Logger LOG = LogManager.getLogger(NotificationClient.class);

    private RestClient restClient;
    private String sourceName;
    private String notificationSetEndpointUrl;
    private String notificationRemoveEndpointUrl;

    /**
     * Creates a new instance.
     *
     * @param controller The current instance
     * @oaram sourceName The source name / identifier for requests from this client
     */
    public NotificationClient(Controller controller, String sourceName)
    {
        this.restClient = new RestClient(sourceName);
        this.sourceName = sourceName;

        if (controller.isComponentEnabled("notification-daemon"))
        {
            this.notificationSetEndpointUrl = controller.getSettings().getString("notification-daemon.notifications.set.url");
            this.notificationRemoveEndpointUrl = controller.getSettings().getString("notification-daemon.notifications.remove.url");
        }
        else
        {
            this.notificationSetEndpointUrl = null;
            this.notificationRemoveEndpointUrl = null;
        }
    }

    /**
     * Updates the current notification.
     *
     * @param notification The current instance
     */
    public void updateNotification(Notification notification)
    {
        // Check notification daemon is available
        if (notificationSetEndpointUrl == null)
        {
            LOG.debug("Ignored request to change notification, notification daemon unavailable - notification: {}", notification);
            return;
        }

        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();

            jsonRoot.put("header", notification.getHeader());
            jsonRoot.put("text", notification.getText());
            jsonRoot.put("lifespan", notification.getLifespan());
            jsonRoot.put("type", notification.getType());

            jsonRoot.put("source", sourceName);
            jsonRoot.put("priority", notification.getPriority());

            // Make request
            restClient.executePost(notificationSetEndpointUrl, jsonRoot);

            LOG.debug("Notification update sent - notification: {}", notification);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to notification daemon - url: {}, notification: {}",
                    notificationSetEndpointUrl, notification
            );
        }
        catch (Exception e)
        {
            LOG.error("Failed to connect to notification daemon - url: {}, notification: {}",
                    notificationSetEndpointUrl, notification, e
            );
        }
    }

    public void removeNotification()
    {
        // Check notification daemon is available
        if (notificationSetEndpointUrl == null)
        {
            LOG.debug("Ignored request to remove notification for current source, notification daemon unavailable - source name: {}", sourceName);
            return;
        }

        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();

            jsonRoot.put("source", sourceName);

            // Make request
            restClient.executePost(notificationRemoveEndpointUrl, jsonRoot);

            LOG.debug("Notification removed for current source - source: {}", sourceName);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to notification daemon - url: {}",
                    notificationSetEndpointUrl
            );
        }
        catch (Exception e)
        {
            LOG.error("Failed to connect to notification daemon - url: {}",
                    notificationSetEndpointUrl, e
            );
        }
    }

}
