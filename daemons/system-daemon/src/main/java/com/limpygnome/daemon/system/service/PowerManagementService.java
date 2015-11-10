package com.limpygnome.daemon.system.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.rest.RestRequest;
import com.limpygnome.daemon.common.rest.RestResponse;
import com.limpygnome.daemon.api.RestServiceHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A power management service.
 */
public class PowerManagementService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(PowerManagementService.class);

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
                default:
                    return false;
            }
        }

        return true;
    }

    public void reboot()
    {
        LOG.info("Rebooting...");
        environmentService.exec("reboot", PROCESS_TIMEOUT);
    }

    public void shutdown()
    {
        LOG.info("Shutting down...");
        environmentService.exec("halt", PROCESS_TIMEOUT);
    }

}
