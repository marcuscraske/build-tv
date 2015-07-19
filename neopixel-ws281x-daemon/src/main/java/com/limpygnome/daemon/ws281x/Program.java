package com.limpygnome.daemon.ws281x;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.ws281x.service.LedService;
import com.limpygnome.daemon.ws281x.service.RestService;

/**
 * The entry point into the daemon.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller();

        // Add services
        controller.add("leds", new LedService());
        controller.add("rest", new RestService());

        // Start forever...
        controller.hookShutdown();
        controller.start();
        controller.waitForExit();
    }
}
