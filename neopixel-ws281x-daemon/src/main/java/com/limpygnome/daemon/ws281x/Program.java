package com.limpygnome.daemon.ws281x;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.ws281x.rest.LedRestHandler;
import com.limpygnome.daemon.ws281x.service.LedService;
import com.limpygnome.daemon.service.RestService;

/**
 * The entry point into the daemon.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller("ws281x-daemon");

        // Add services
        controller.add("leds", new LedService());

        // Add REST handlers
        RestService.addRestHandlerToControllerRuntime(controller, new LedRestHandler());

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }
}
