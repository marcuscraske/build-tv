package com.limpygnome.ws281x.daemon;

import com.limpygnome.ws281x.daemon.service.imp.LedService;
import com.limpygnome.ws281x.daemon.service.imp.RestService;

/**
 * The entry point into the daemon.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller();

        // Add services
        //controller.add("leds", new LedService());
        controller.add("rest", new RestService());

        // Start forever...
        controller.hookShutdown();
        controller.start();
        controller.waitForExit();
    }
}
