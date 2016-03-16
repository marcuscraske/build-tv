package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.Settings;
import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A service used to provide the hostname and REST port of this daemon.
 */
public class HostInformationService implements Service
{
    private static final Logger LOG = LogManager.getLogger(HostInformationService.class);

    public static final String SERVICE_NAME = "host-information";

    private String hostname;
    private long restPort;

    @Override
    public void start(Controller controller)
    {
        Settings settingsForwarder = new Settings(controller, StatsForwarderService.STATS_FORWARDER_CONFIG_FILENAME);

        // Attempt to use overridden/forced hostname
        hostname = settingsForwarder.getString("hostname");

        if (hostname == null  || hostname.length() == 0 || hostname.equals("auto") || hostname.equals("*"))
        {
            this.hostname = EnvironmentUtil.getIpAddress();
        }

        // Check we have something...
        if (this.hostname == null)
        {
            throw new RuntimeException("Unable to determine hostname, use stats-forwarder.json -> 'hostname' key to force hostname instead");
        }

        // Read REST port
        this.restPort = controller.getSettings().getLong("rest/port");

        LOG.info("host information service setup - hostname: {}, port: {}", hostname, restPort);
    }

    @Override
    public void stop(Controller controller)
    {
        this.hostname = null;
    }

    public String getHostname()
    {
        return hostname;
    }

    public long getRestPort()
    {
        return restPort;
    }

}
