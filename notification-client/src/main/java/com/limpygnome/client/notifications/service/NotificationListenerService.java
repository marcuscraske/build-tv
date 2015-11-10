package com.limpygnome.client.notifications.service;

import com.limpygnome.client.notifications.subscriber.NotificationListenerThread;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A service used to manage the thread for listening to a notifications endpoint.
 */
public class NotificationListenerService implements Service
{
    private static final Logger LOG = LogManager.getLogger(NotificationListenerService.class);

    public static final String SERVICE_NAME = "notifications-listener";

    private NotificationListenerThread notificationListenerThread;

    public NotificationListenerService()
    {
        this.notificationListenerThread = null;
    }

    @Override
    public void start(Controller controller)
    {
        // Check client is even enabled
        if (!controller.getSettings().getBoolean("notifications/enabled"))
        {
            throw new RuntimeException("Notifications client not enabled, aborting startup");
        }

        // Fetch endpoint
        String notificationsEndpoint = controller.getSettings().getString("notifications/endpoint");
        LOG.debug("Notifications endpoint - url: {}", notificationsEndpoint);

        // Start thread
        notificationListenerThread = new NotificationListenerThread(notificationsEndpoint);
        notificationListenerThread.start();
    }

    @Override
    public void stop(Controller controller)
    {
        if (notificationListenerThread != null)
        {
            notificationListenerThread.kill();
            notificationListenerThread = null;
        }
    }

}
