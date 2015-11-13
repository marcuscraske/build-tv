package com.limpygnome.client.notification;

import com.limpygnome.client.notification.service.NotificationListenerService;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.imp.DefaultController;

/**
 * Entry point for the notification client.
 */
public class Program
{

    public static void main(String[] args)
    {
        Controller controller = new DefaultController("notifications-client");

        // Add services
        controller.add(NotificationListenerService.SERVICE_NAME, new NotificationListenerService());

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}
