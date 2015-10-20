package com.limpygnome.daemon.remote;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.remote.service.HostInformationService;
import com.limpygnome.daemon.remote.service.InstanceIdentityService;
import com.limpygnome.daemon.remote.service.RestProxyService;
import com.limpygnome.daemon.remote.service.StatsForwarderService;
import com.limpygnome.daemon.remote.service.VersionService;
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
        controller.add(RandomKeyAuthProviderService.SERVICE_NAME, new RandomKeyAuthProviderService());
        controller.add(RestProxyService.SERVICE_NAME, new RestProxyService());
        controller.add(InstanceIdentityService.SERVICE_NAME, new InstanceIdentityService());
        controller.add(VersionService.SERVICE_NAME, new VersionService());
        controller.add(HostInformationService.SERVICE_NAME, new HostInformationService());
        controller.add(StatsForwarderService.SERVICE_NAME, new StatsForwarderService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}
