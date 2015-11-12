package com.limpygnome.daemon.interval;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.imp.DefaultController;
import com.limpygnome.daemon.interval.service.IntervalService;
import com.limpygnome.daemon.interval.service.NotificationService;
import com.limpygnome.daemon.service.RestService;

/**
 * Entry point into the interval daemon.
 */
public class Program
{

    public static void main(String[] args)
    {
        Controller controller = new DefaultController("interval-daemon");

        // Add services
        controller.add(IntervalService.SERVICE_NAME, new IntervalService());
        controller.add(NotificationService.SERVICE_NAME, new NotificationService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}
