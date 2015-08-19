package com.limpygnome.daemon.led;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.led.rest.LedRestHandler;
import com.limpygnome.daemon.led.service.LedService;
import com.limpygnome.daemon.service.RestService;

/**
 * The entry point into the LED daemon.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller("led-daemon");

        // Add services
        controller.add("leds", new LedService());

        // Add REST handlers
        RestService.addRestHandlerToControllerRuntime(controller, new LedRestHandler());

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }
}
