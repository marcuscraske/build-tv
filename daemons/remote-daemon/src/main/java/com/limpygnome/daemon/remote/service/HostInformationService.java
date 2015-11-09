package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.util.EnvironmentUtil;

/**
 * A service used to provide the hostname and REST port of this daemon.
 */
public class HostInformationService implements Service
{
    public static final String SERVICE_NAME = "host-information";

    private String hostname;
    private long restPort;

    @Override
    public void start(Controller controller)
    {
        // Retrieve hostname for this instance
        this.hostname = EnvironmentUtil.getIpAddress();

        if (this.hostname == null)
        {
            this.hostname = "null-route-service";
        }

        // Read REST port
        this.restPort = controller.getSettings().getLong("rest/port");
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
