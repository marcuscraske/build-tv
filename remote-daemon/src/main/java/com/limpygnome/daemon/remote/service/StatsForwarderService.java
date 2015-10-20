package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.remote.service.thread.StatsForwarderThread;

/**
 * A service which takes the stats from this daemon and posts them to an external REST endpoint.
 */
public class StatsForwarderService implements Service
{
    public static final String SERVICE_NAME = "stats-forwarder";

    private long frequency;
    private String endpointUrlInfo;
    private String endpointUrlUpdate;

    private StatsForwarderThread statsForwarderThread;

    @Override
    public void start(Controller controller)
    {
        frequency = controller.getSettings().getLong("stats-forwarder/frequency");
        endpointUrlInfo = controller.getSettings().getString("stats-forwarder/url/info");
        endpointUrlUpdate = controller.getSettings().getString("stats-forwarder/url/update");

        // Startup forwarder thread
        statsForwarderThread = new StatsForwarderThread(controller, this);
        statsForwarderThread.start();
    }

    @Override
    public void stop(Controller controller)
    {
        // Stop forwarder thread
        statsForwarderThread.kill();
        statsForwarderThread = null;

        endpointUrlInfo = null;
        endpointUrlUpdate = null;
    }

    public long getFrequency()
    {
        return frequency;
    }

    public String getEndpointUrlInfo()
    {
        return endpointUrlInfo;
    }

    public String getEndpointUrlUpdate()
    {
        return endpointUrlUpdate;
    }

}
