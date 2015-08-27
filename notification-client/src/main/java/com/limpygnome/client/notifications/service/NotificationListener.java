package com.limpygnome.client.notifications.service;

import com.limpygnome.client.notifications.subscriber.NotificationListenerThread;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by limpygnome on 26/08/15.
 */
public class NotificationListener implements Service
{
    private static final Logger LOG = LogManager.getLogger(NotificationListener.class);

    private NotificationListenerThread notificationListenerThread;

    public NotificationListener()
    {
        this.notificationListenerThread = null;
    }

    @Override
    public void start(Controller controller)
    {
        String notificationsEndpoint = controller.getSettings().getString("notifications/endpoint");
        LOG.debug("Notifications endpoint - url: {}", notificationsEndpoint);

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
