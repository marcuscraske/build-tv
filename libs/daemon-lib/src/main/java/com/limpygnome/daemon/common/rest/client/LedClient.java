package com.limpygnome.daemon.common.rest.client;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.LedPattern;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.net.ConnectException;

/**
 * REST client used to communicate with LED daemon to update LED pattern.
 */
public class LedClient
{
    private static final Logger LOG = LogManager.getLogger(LedClient.class);

    private RestClient restClient;
    private String sourceName;
    private String ledEndpointUrl;

    /**
     * Creates a new instance.
     *
     * @param controller The current controller
     * @param sourceName The source name / unique identifier for LED changes
     */
    public LedClient(Controller controller, String sourceName)
    {
        if (sourceName == null || sourceName.length() == 0)
        {
            throw new IllegalArgumentException("Source name must be specified!");
        }

        this.restClient = new RestClient(sourceName);
        this.sourceName = sourceName;

        // Check LED daemon is available
        if (controller.isComponentEnabled("led-daemon"))
        {
            this.ledEndpointUrl = controller.getSettings().getString("led-daemon.rest.url");

            LOG.warn("Any requests for LED daemon will be ignored, since the daemon is not enabled");
        }
        else
        {
            this.ledEndpointUrl = null;
        }
    }

    /**
     * Changes the LED pattern for the current source, using the default priority for a pattern.
     *
     * @param pattern The new pattern, or null to remove this source for LED patterns
     */
    public void changeLedPattern(LedPattern pattern)
    {
        changeLedPattern(pattern, pattern != null ? pattern.PRIORITY : 0);
    }

    public void changeLedPattern(LedPattern pattern, int priority)
    {
        // Check LED daemon is available...
        if (ledEndpointUrl == null)
        {
            LOG.debug("Ignoring pattern change request, LED daemon unavailable - pattern: {}", pattern.PATTERN);
            return;
        }

        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();
            jsonRoot.put("source", sourceName);
            jsonRoot.put("pattern", pattern != null ? pattern.PATTERN : "");
            jsonRoot.put("priority", priority);

            // Make request
            restClient.executePost(ledEndpointUrl, jsonRoot);

            LOG.debug("LED daemon update request sent - pattern: {}", pattern.PATTERN);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to LED daemon - url: {}, pattern: {}", ledEndpointUrl, pattern.PATTERN);
        }
        catch (Exception e)
        {
            LOG.error("Failed to make LED daemon request", e);
        }
    }

    /**
     * Removes this client as a source.
     */
    public void removeSource()
    {
        changeLedPattern(null);
    }

}
