package com.limpygnome.daemon.led.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.limpygnome.daemon.api.rest.RestResponse;
import com.limpygnome.daemon.api.rest.RestServiceHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A REST service/handler to handle requests to control the {@link LedService}.
 */
public class LedRestService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(LedRestService.class);

    private LedService ledService;


    @Override
    public void start(Controller controller)
    {
        ledService = (LedService) controller.getServiceByName("leds");
    }

    @Override
    public void stop(Controller controller)
    {
        ledService = null;
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        if (!restRequest.isJsonRequest() || !restRequest.isPathMatch(new String[]{ "led-daemon", "leds" }))
        {
            return false;
        }

        // Check required data elements present
        String source = (String) restRequest.getJsonElement(new String[]{ "source" });
        String pattern = (String) restRequest.getJsonElement(new String[]{ "pattern" });
        Long priority = (Long) restRequest.getJsonElement(new String[]{ "priority" });

        if (source == null || pattern == null || priority == null)
        {
            LOG.debug("Malformed LED daemon request");
            return false;
        }

        // Read, clean and validate requested LED pattern
        pattern = pattern.trim().toLowerCase();

        if (!source.matches("^[a-z0-9\\_\\-]+$"))
        {
            throw new IllegalArgumentException("Invalid source specified in web request");
        }
        else if (!pattern.matches("^[a-z0-9\\_\\-]+$"))
        {
            throw new IllegalArgumentException("Invalid pattern specified in web request");
        }

        // Hand to LED service to use pattern
        if (ledService != null)
        {
            // Check if to remove pattern based on priority being zero
            if (priority == 0)
            {
                LOG.debug("Removing pattern - source: {}", source);

                ledService.removeLedSource(source);
            }
            else
            {
                LOG.debug("Attempting pattern change - source: {}, pattern: {}, priority: {}",
                        source, pattern, priority
                );

                ledService.setLedSource(source, pattern, priority);
            }
        }
        else
        {
            LOG.warn("Unable to set LED pattern, LED service not available - source: {}, pattern: {}, priority: {}",
                    source, pattern, priority
            );
        }

        return true;
    }

}
