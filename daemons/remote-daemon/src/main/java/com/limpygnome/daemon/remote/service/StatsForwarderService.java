package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.api.Settings;
import com.limpygnome.daemon.remote.service.thread.StatsForwarderThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A service which takes the stats from this daemon and posts them to an external REST endpoint.
 */
public class StatsForwarderService implements Service
{
    private static final Logger LOG = LogManager.getLogger(StatsForwarderService.class);

    public static final String SERVICE_NAME = "stats-forwarder";

    private static final String STATS_FORWARDER_CONFIG_FILENAME = "stats-forwarder.json";

    private long frequency;
    private String endpointUrlInfo;
    private String endpointUrlUpdate;

    private StatsForwarderThread statsForwarderThread;

    @Override
    public void start(Controller controller)
    {
        Settings settingsForwarder = new Settings();
        settingsForwarder.reload(controller, STATS_FORWARDER_CONFIG_FILENAME);

        // Check service is enabled
        boolean enabled = settingsForwarder.getBoolean("enabled");

        if (enabled)
        {
            LOG.info("Enabling stats forwarding...");

            frequency = settingsForwarder.getLong("frequency");
            endpointUrlInfo = settingsForwarder.getString("url/info");
            endpointUrlUpdate = settingsForwarder.getString("url/update");

            // Startup forwarder thread
            statsForwarderThread = new StatsForwarderThread(controller, this);
            statsForwarderThread.start();
        }
        else
        {
            LOG.info("Stats forwarding disabled");
        }
    }

    @Override
    public void stop(Controller controller)
    {
        // Stop forwarder thread
        if (statsForwarderThread != null)
        {
            statsForwarderThread.kill();
            statsForwarderThread = null;
        }

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
