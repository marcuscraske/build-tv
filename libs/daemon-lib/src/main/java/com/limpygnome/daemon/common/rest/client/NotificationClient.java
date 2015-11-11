package com.limpygnome.daemon.common.rest.client;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Notification;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.awt.*;
import java.net.ConnectException;

/**
 * REST client used to communicate with interval daemon for notifications.
 */
public class NotificationClient
{
    private static final Logger LOG = LogManager.getLogger(NotificationClient.class);

    private RestClient restClient;
    private String sourceName;
    private String notificationEndpointUrl;

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

        if (controller.isDaemonEnabled("interval-daemon"))
        {
            this.notificationEndpointUrl = controller.getSettings().getString("interval-daemon.notifications.url");
        }
        else
        {
            notificationEndpointUrl = null;
        }
    }

    /**
     * Updates the current notification.
     *
     * @param notification The current instance
     */
    public void updateNotification(Notification notification)
    {
        // Check interval daemon is available
        if (notificationEndpointUrl == null)
        {
            LOG.debug("Ignored request to change notification, interval daemon unavailable - notification: {}", notification);
            return;
        }

        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();

            jsonRoot.put("header", notification.getHeader());
            jsonRoot.put("text", notification.getText());
            jsonRoot.put("lifespan", notification.getLifespan());

            Color background = notification.getBackground();
            jsonRoot.put("backgroundR", background.getRed());
            jsonRoot.put("backgroundG", background.getGreen());
            jsonRoot.put("backgroundB", background.getBlue());

            jsonRoot.put("source", sourceName);
            jsonRoot.put("priority", notification.getPriority());

            // Make request
            restClient.executePost(notificationEndpointUrl, jsonRoot);

            LOG.debug("Notification update sent - notification: {}", notification);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to interval daemon - url: {}, notification: {}",
                    notificationEndpointUrl, notification
            );
        }
        catch (Exception e)
        {
            LOG.error("Failed to connect to interval daemon - url: {}, notification: {}",
                    notificationEndpointUrl, notification, e
            );
        }
    }

}
