package com.limpygnome.daemon.buildtv;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.buildtv.service.*;
import com.limpygnome.daemon.service.RestService;

/**
 * Entry point into the build TV daemon.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller("build-tv-daemon");

        // Add services
        controller.add(LedTimeService.SERVICE_NAME, new LedTimeService());
        controller.add(JenkinsService.SERVICE_NAME, new JenkinsService());
        controller.add(IntervalLedService.SERVICE_NAME, new IntervalLedService(controller));
        controller.add(NotificationService.SERVICE_NAME, new NotificationService());
        controller.add(JiraDashboardService.SERVICE_NAME, new JiraDashboardService());
        controller.add(HardwareCommsService.SERVICE_NAME, new HardwareCommsService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }
}
