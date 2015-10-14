package com.limpygnome.daemon.remote;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.remote.service.IntervalUpdateService;
import com.limpygnome.daemon.remote.service.RestProxyService;
import com.limpygnome.daemon.remote.service.auth.RandomKeyAuthProviderService;
import com.limpygnome.daemon.service.RestService;

/**
 * Entry point into the remote daemon.
 */
public class Program
{

    public static void main(String[] args)
    {
        Controller controller = new Controller("remote-daemon");

        // Add services
        controller.add("updates", new IntervalUpdateService());
        controller.add("auth", new RandomKeyAuthProviderService());
        controller.add("forwarder", new RestProxyService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}
