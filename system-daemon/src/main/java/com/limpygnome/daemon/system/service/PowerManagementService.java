package com.limpygnome.daemon.system.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.limpygnome.daemon.api.rest.RestResponse;
import com.limpygnome.daemon.api.rest.RestServiceHandler;

/**
 * A power management service.
 */
public class PowerManagementService implements Service, RestServiceHandler
{
    public static final String SERVICE_NAME = "power-management";

    private EnvironmentService environmentService;

    private static final long PROCESS_TIMEOUT = 2000;

    @Override
    public void start(Controller controller)
    {
        environmentService = (EnvironmentService) controller.getServiceByName("environment");
    }

    @Override
    public void stop(Controller controller)
    {
        environmentService = null;
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        // Check we can handle the request
        if (!restRequest.isJsonRequest() || !restRequest.isPathMatch(new String[]{ "system-daemon", "power" }))
        {
            return false;
        }

        // Check request has JSON body
        String action = (String) restRequest.getJsonElement(new String[]{ "action" });

        if (action != null && action.length() > 0)
        {
            switch (action)
            {
                case "reboot":
                    reboot();
                    break;
                case "shutdown":
                    shutdown();
                    break;
            }
        }

        return true;
    }

    public void reboot()
    {
        environmentService.exec("reboot", PROCESS_TIMEOUT);
    }

    public void shutdown()
    {
        environmentService.exec("halt", PROCESS_TIMEOUT);
    }

}
