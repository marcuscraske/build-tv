package com.limpygnome.daemon.screen;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.screen.rest.SystemRestHandler;
import com.limpygnome.daemon.screen.service.EnvironmentService;
import com.limpygnome.daemon.screen.service.PowerService;
import com.limpygnome.daemon.screen.service.ScreenService;
import com.limpygnome.daemon.service.RestService;

/**
 * Entry point into the system daemon.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller("system-daemon");

        // Add services
        controller.add("environment", new EnvironmentService());
        controller.add("screen", new ScreenService());
        controller.add("power", new PowerService());

        // Add REST handlers
        RestService.addRestHandlerToControllerRuntime(controller, new SystemRestHandler());

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }
}
