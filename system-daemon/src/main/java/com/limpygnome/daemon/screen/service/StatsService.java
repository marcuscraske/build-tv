package com.limpygnome.daemon.screen.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.limpygnome.daemon.api.rest.RestResponse;
import com.limpygnome.daemon.api.rest.RestServiceHandler;
import com.limpygnome.daemon.screen.model.stat.Statistic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Retrieves statistics for the system, which can be reported to an external source.
 */
public class StatsService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(EnvironmentService.class);

    private EnvironmentService environmentService;
    private long statUpdateTimeout;
    private float lastPolled;

    @Override
    public void start(Controller controller)
    {
        // Fetch environment service
        environmentService = (EnvironmentService) controller.getServiceByName(EnvironmentService.SERVICE_NAME);

        // Read stat update timeout setting


        // Reset last polled
        lastPolled = 0;
    }

    @Override
    public void stop(Controller controller)
    {
        environmentService = null;
    }

    private void updateStats()
    {
        // Check if we've recently updated
        if (System.currentTimeMillis() - lastPolled > statUpdateTimeout)
        {
        }
    }

    public Statistic[] poll()
    {
        updateStats();

        // Build result
        List<Statistic> stats = new LinkedList<>();

        return stats.toArray(new Statistic[stats.size()]);
    }

    private float getCpuPercent()
    {

        return environmentService.execute(BASH_COMMANDS);
    }

    private float getMemoryPercent()
    {
        final String BASH_COMMANDS = "free -m| grep  Mem | awk '{ print int($3/$2*100) }'";
        return environmentService.execute(BASH_COMMANDS);
    }

    private float getTemperatureCelcius()
    {
        final String BASH_COMMANDS = "expr substr \"$(cat /sys/class/thermal/thermal_zone0/temp)\" 1 2";
        return environmentService.execute(BASH_COMMANDS);
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        if (!restRequest.isPathMatch(new String[]{ "system-daemon", "stats" })) {
            return false;
        }
    }

    public EnvironmentService getEnvironmentService()
    {
        return environmentService;
    }

}
