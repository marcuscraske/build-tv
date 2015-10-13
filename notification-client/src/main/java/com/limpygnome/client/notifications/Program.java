package com.limpygnome.client.notifications;

import com.limpygnome.client.notifications.service.NotificationListener;
import com.limpygnome.daemon.api.Controller;

/**
 * Created by limpygnome on 26/08/15.
 */
public class Program
{

    public static void main(String[] args)
    {
        Controller controller = new Controller("notifications-client");

        // Add services
        controller.add("notifications-listener", new NotificationListener());

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}