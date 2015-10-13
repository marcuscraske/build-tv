package com.limpygnome.daemon.screen.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;

/**
 * Created by limpygnome on 13/10/15.
 */
public class PowerService implements Service
{
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

    public void reboot()
    {
        environmentService.exec("reboot", PROCESS_TIMEOUT);
    }

    public void shutdown()
    {
        environmentService.exec("halt", PROCESS_TIMEOUT);
    }

}
