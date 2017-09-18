package com.limpygnome.daemon.buildstatus;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.imp.DefaultController;
import com.limpygnome.daemon.buildstatus.service.*;
import com.limpygnome.daemon.service.RestService;

/**
 * Entry point into the build status daemon.
 */
public class Program
{

    public static void main(String[] args)
    {
        Controller controller = new DefaultController("build-status-daemon");

        // Add services
        controller.add(JenkinsService.SERVICE_NAME, new JenkinsService());
        controller.add(BuildStatusService.SERVICE_NAME, new BuildStatusService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}
