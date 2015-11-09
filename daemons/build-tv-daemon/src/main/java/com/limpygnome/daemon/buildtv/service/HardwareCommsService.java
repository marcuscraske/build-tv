package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.buildtv.led.ScreenAction;
import com.limpygnome.daemon.buildtv.led.pattern.LedPattern;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.net.ConnectException;

/**
 * A service for interfacing with external hardware.
 *
 * This daemon is not expected to handle any hardware, but send off requests to other daemons.
 */
public class HardwareCommsService  implements Service
{
    private static final Logger LOG = LogManager.getLogger(HardwareCommsService.class);

    public static final String SERVICE_NAME = "hardware-comms";

    private static final String LED_DAEMON_SOURCE = "build-tv";

    private String ledDaemonUrlLeds;
    private long ledDaemonPriority;

    @Override
    public void start(Controller controller)
    {
        // Check LED daemon is available
        if (controller.isDaemonEnabled("led-daemon"))
        {
            this.ledDaemonUrlLeds = controller.getSettings().getString("led-daemon.rest.url");
            this.ledDaemonPriority = controller.getSettings().getLong("led-daemon.priority");
        }
        else
        {
            this.ledDaemonUrlLeds = null;
        }
    }

    @Override
    public void stop(Controller controller)
    {
        // Clear daemon URLs
        ledDaemonUrlLeds = null;
    }

    public void changeLedPattern(LedPattern pattern)
    {
        // Check LED daemon is available...
        if (ledDaemonUrlLeds == null)
        {
            LOG.debug("Ignoring pattern change request, LED daemon unavailable - pattern: {}", pattern.PATTERN);
            return;
        }

        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();
            jsonRoot.put("source", LED_DAEMON_SOURCE);
            jsonRoot.put("pattern", pattern.PATTERN);
            jsonRoot.put("priority", ledDaemonPriority);

            // Make request
            RestClient restClient = new RestClient();
            restClient.executePost(ledDaemonUrlLeds, jsonRoot);

            LOG.debug("LED daemon update request sent - pattern: {}", pattern.PATTERN);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to LED daemon - url: {}, pattern: {}", ledDaemonUrlLeds, pattern.PATTERN);
        }
        catch (Exception e)
        {
            LOG.error("Failed to make LED daemon request", e);
        }
    }

    public void changeScreen(Controller controller, ScreenAction screenAction)
    {
        // Check daemon is available
        if (controller.isDaemonEnabled("system-daemon"))
        {
            // Fetch system-daemon screen endpoint
            String systemDaemonScreenEndpoint = controller.getSettings().getString("system-daemon.screen.rest.url");

            try
            {
                // Build JSON object
                JSONObject jsonRoot = new JSONObject();
                jsonRoot.put("action", screenAction.ACTION);

                // Make request
                RestClient restClient = new RestClient();
                restClient.executePost(systemDaemonScreenEndpoint, jsonRoot);

                LOG.debug("Screen action sent - action: {}", screenAction);
            }
            catch (ConnectException e)
            {
                LOG.error("Failed to connect to system daemon - url: {}", systemDaemonScreenEndpoint);
            }
            catch (Exception e)
            {
                LOG.error("Failed to make system daemon request", e);
            }
        }
        else
        {
            LOG.debug("Ignored request to change screen, screen daemon unavailable - screen-action: {}", screenAction);
        }
    }

}
