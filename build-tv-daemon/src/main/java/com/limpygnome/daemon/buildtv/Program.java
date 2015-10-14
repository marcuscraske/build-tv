package com.limpygnome.daemon.buildtv;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.buildtv.service.IntervalLedService;
import com.limpygnome.daemon.buildtv.service.JenkinsService;
import com.limpygnome.daemon.buildtv.service.LedTimeService;
import com.limpygnome.daemon.buildtv.service.NotificationService;
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
        controller.add("led-time", new LedTimeService());
        controller.add("jenkins-status", new JenkinsService());
        controller.add("interval-leds", new IntervalLedService(controller));
        controller.add(NotificationService.SERVICE_NAME, new NotificationService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }
}
