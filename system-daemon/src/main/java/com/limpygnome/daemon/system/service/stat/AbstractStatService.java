package com.limpygnome.daemon.system.service.stat;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.system.model.stat.Statistic;
import com.limpygnome.daemon.system.service.EnvironmentService;

/**
 * Used to implement a generic statistic service.
 *
 * This allows a system metric, or a measurable item, to be reported.
 */
public abstract class AbstractStatService implements Service
{
    /**
     * Default timeout for processes used to retrieve system metrics.
     */
    protected final long DEFAULT_PROCESS_TIMEOUT = 5000;
    protected EnvironmentService environmentService;

    @Override
    public void start(Controller controller)
    {
        environmentService = (EnvironmentService) controller.getServiceByName(EnvironmentService.SERVICE_NAME);
    }

    @Override
    public void stop(Controller controller)
    {
        environmentService = null;
    }

    public abstract Statistic update();

}
