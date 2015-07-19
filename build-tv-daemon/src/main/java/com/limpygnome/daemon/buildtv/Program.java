package com.limpygnome.daemon.buildtv;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.buildtv.service.IntervalLedService;
import com.limpygnome.daemon.buildtv.service.JenkinsService;
import com.limpygnome.daemon.buildtv.service.LedTimeService;

/**
 * Created by limpygnome on 19/07/15.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller();

        // Add services
        controller.add("led-time", new LedTimeService());
        controller.add("jenkins-status", new JenkinsService());
        controller.add("interval-leds", new IntervalLedService());

        // Start forever...
        controller.hookShutdown();
        controller.start();
        controller.waitForExit();
    }
}
