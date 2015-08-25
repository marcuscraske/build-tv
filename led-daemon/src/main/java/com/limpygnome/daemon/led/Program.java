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
    enum Mode
    {
        DAEMON,
        LED_TEST
    }


    public static void main(String[] args)
    {
        // Determine mode of application
        Mode mode = Mode.DAEMON;

        if (args != null && args.length > 0)
        {
            if (args[0] != null && args[0].equals("test"))
            {
                mode = Mode.LED_TEST;
            }
        }

        // Handle mode
        switch (mode)
        {
            case DAEMON:
                runDaemon();
                break;
            case LED_TEST:
                runLedTest();
                break;
        }
    }

    public static void runDaemon()
    {
        Controller controller = new Controller("led-daemon");

        // Add services
        controller.add("leds", new LedService());

        // Add REST handlers
        RestService.addRestHandlerToControllerRuntime(controller, new LedRestHandler());

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

    public static void runLedTest()
    {
        // Setup REST client to set test/diagnostics pattern
        // TODO: finish this
    }

}
