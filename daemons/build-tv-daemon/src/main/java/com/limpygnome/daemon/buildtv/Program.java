package com.limpygnome.daemon.buildtv;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.imp.DefaultController;
import com.limpygnome.daemon.buildtv.service.*;
import com.limpygnome.daemon.service.RestService;

/**
 * Entry point into the build TV daemon.
 */
public class Program
{

    public static void main(String[] args)
    {
        Controller controller = new DefaultController("build-tv-daemon");

        // Add services
        controller.add(JenkinsService.SERVICE_NAME, new JenkinsService());
        controller.add(JiraDashboardService.SERVICE_NAME, new JiraDashboardService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}
