package com.limpygnome.client.launcher;

import com.limpygnome.client.launcher.service.LauncherService;
import com.limpygnome.client.launcher.service.WebServerService;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.imp.DefaultController;
import com.limpygnome.daemon.service.RestService;

/**
 * Entry point for the notification client.
 */
public class Program
{

    public static void main(String[] args)
    {
        Controller controller = new DefaultController("launcher-client");

        // Add services
        controller.add(LauncherService.SERVICE_NAME, new LauncherService());
        controller.add(WebServerService.SERVICE_NAME, new WebServerService());

        // Attach any REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}
