package com.limpygnome.daemon.interval.led;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.common.rest.client.LedClient;
import com.limpygnome.daemon.common.rest.client.NotificationClient;
import com.limpygnome.daemon.common.rest.client.ScreenClient;

/**
 * Holds an aggregate of all clients.
 */
public class ClientAggregate
{
    private final LedClient ledClient;
    private final ScreenClient screenClient;

    public ClientAggregate(Controller controller, String sourceName)
    {
        this.ledClient = new LedClient(controller, sourceName);
        this.screenClient = new ScreenClient(controller, sourceName);
    }

    public LedClient getLedClient()
    {
        return ledClient;
    }

    public ScreenClient getScreenClient()
    {
        return screenClient;
    }

}
