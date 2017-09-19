package com.limpygnome.buildtv.daemon.dashboard;

import com.limpygnome.buildtv.daemon.dashboard.service.DashboardService;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.imp.DefaultController;
import com.limpygnome.daemon.service.RestService;

/**
 * Entry point for dashboard daemon.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new DefaultController("dashboard-daemon");

        // Add services
        controller.add(DashboardService.SERVICE_NAME, new DashboardService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }
}
