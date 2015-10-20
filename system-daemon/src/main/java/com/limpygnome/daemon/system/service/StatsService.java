package com.limpygnome.daemon.system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.limpygnome.daemon.api.rest.RestResponse;
import com.limpygnome.daemon.api.rest.RestServiceHandler;
import com.limpygnome.daemon.system.model.stat.Statistic;
import com.limpygnome.daemon.system.service.stat.AbstractStatService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Retrieves statistics for the system, which can be reported to an external source.
 */
public class StatsService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(EnvironmentService.class);

    public static final String SERVICE_NAME = "stats";

    private Controller controller;
    private EnvironmentService environmentService;
    private long frequency;
    private long lastPolled;

    private Statistic[] cachedStatistics;

    @Override
    public void start(Controller controller)
    {
        this.controller = controller;

        // Fetch environment service
        environmentService = (EnvironmentService) controller.getServiceByName(EnvironmentService.SERVICE_NAME);

        // Read frequency setting
        frequency = controller.getSettings().getOptionalLong("stats/frequency", 5000);

        // Reset last polled
        lastPolled = 0;
    }

    @Override
    public void stop(Controller controller)
    {
        environmentService = null;
    }

    private void updateStats(Controller controller)
    {
        // Check if we've recently updated
        if (System.currentTimeMillis() - lastPolled >= frequency)
        {
            // Update last polled to now
            lastPolled = System.currentTimeMillis();


            // Iterate each service, find stat services
            Map<String, Service> services = controller.getServices();
            Service service;
            AbstractStatService statService;

            List<Statistic> stats = new LinkedList<>();

            for (Map.Entry<String, Service> kv : services.entrySet())
            {
                service = kv.getValue();

                if (service instanceof AbstractStatService)
                {
                    statService = (AbstractStatService) service;
                    stats.add(statService.update());
                }
            }

            cachedStatistics = stats.toArray(new Statistic[stats.size()]);
        }
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        // Check request destined for us
        if (!restRequest.isPathMatch(new String[]{ "system-daemon", "stats" })) {
            return false;
        }

        // Check if stats need updating
        updateStats(controller);

        // Output stats as json
        String json;

        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.writeValueAsString(cachedStatistics);
        }
        catch (JsonProcessingException e)
        {
            LOG.error("Failed to convert stats into json", e);

            json = "{}";
        }

        // Write to response
        restResponse.writeResponseIgnoreExceptions(restResponse, json);
        return true;
    }

    public EnvironmentService getEnvironmentService()
    {
        return environmentService;
    }

    public Statistic[] getCachedStatistics()
    {
        return cachedStatistics;
    }

}
