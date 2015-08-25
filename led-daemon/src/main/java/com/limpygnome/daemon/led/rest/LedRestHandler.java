package com.limpygnome.daemon.led.rest;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.led.service.LedService;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * Handles REST requests for setting the LED pattern.
 */
public class LedRestHandler implements RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(LedRestHandler.class);

    private LedService ledService;

    @Override
    public synchronized void start(Controller controller)
    {
        // Fetch LED service
        ledService = (LedService) controller.getServiceByName("leds");
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        ledService = null;
    }

    @Override
    public boolean handleRequestInChain(HttpExchange httpExchange, JSONObject jsonRoot)
    {
        // Check required data elements present
        if (!jsonRoot.containsKey("source") || !jsonRoot.containsKey("pattern") || !jsonRoot.containsKey("priority"))
        {
            LOG.debug("Malformed LED daemon request");
            return false;
        }

        // Read, clean and validate requested LED pattern
        String source = (String) jsonRoot.get("source");
        String pattern = (String) jsonRoot.get("pattern");
        long priority = (long) jsonRoot.get("priority");

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
            LOG.debug("Attempting pattern change - source: {}, pattern: {}, priority: {}",
                    source, pattern, priority
            );

            ledService.setPattern(source, pattern, priority);
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
