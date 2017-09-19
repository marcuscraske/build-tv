package com.limpygnome.client.launcher;

import com.limpygnome.client.launcher.service.DashboardBrowserService;
import com.limpygnome.client.launcher.service.WebServerService;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.imp.DefaultController;
import com.limpygnome.daemon.service.RestService;

/**
 * Entry point for the notification client.
 */
public class ProgramLauncherClient
{

    public static void main(String[] args)
    {
        Controller controller = new DefaultController("launcher-client");

        // Add services
        controller.add(WebServerService.SERVICE_NAME, new WebServerService());
        controller.add(DashboardBrowserService.SERVICE_NAME, new DashboardBrowserService());

        // Attach any REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}
