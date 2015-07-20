package com.limpygnome.daemon.screen;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.screen.rest.ScreenRestHandler;
import com.limpygnome.daemon.screen.service.ScreenService;
import com.limpygnome.daemon.service.RestService;

/**
 * Created by limpygnome on 20/07/15.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller("screen-daemon");

        // Add services
        controller.add("screen", new ScreenService());

        // Add REST handlers
        RestService.addRestHandlerToControllerRuntime(controller, new ScreenRestHandler());

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }
}
