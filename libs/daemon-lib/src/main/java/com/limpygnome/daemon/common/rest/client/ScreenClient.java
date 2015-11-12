package com.limpygnome.daemon.common.rest.client;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ScreenAction;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.net.ConnectException;

/**
 * REST client used to communicate with system daemon to change screen.
 */
public class ScreenClient
{
    private static final Logger LOG = LogManager.getLogger(ScreenClient.class);

    private RestClient restClient;
    private String screenEndpointUrl;

    /**
     *
     * @param controller
     */
    public ScreenClient(Controller controller)
    {
        this(controller, null);
    }

    /**
     * Creates a new instance.
     *
     * @param controller The current controller
     * @param sourceName The source name / unique identifier for requests
     */
    public ScreenClient(Controller controller, String sourceName)
    {
        this.restClient = new RestClient(sourceName);

        if (controller.isDaemonEnabled("system-daemon"))
        {
            // Fetch system-daemon screen endpoint
            screenEndpointUrl = controller.getSettings().getString("system-daemon.screen.rest.url");
        }
        else
        {
            screenEndpointUrl = null;
        }
    }

    /**
     * Changes the attached screen / VHD.
     *
     * @param controller The current controller
     * @param screenAction The action to apply
     */
    public void changeScreen(Controller controller, ScreenAction screenAction)
    {
        // Check system daemon is available
        if (screenEndpointUrl == null)
        {
            LOG.debug("Ignored request to change screen, screen daemon unavailable - screen-action: {}", screenAction);
            return;
        }

        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();
            jsonRoot.put("action", screenAction.ACTION);

            // Make request
            restClient.executePost(screenEndpointUrl, jsonRoot);

            LOG.debug("Screen action sent - action: {}", screenAction);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to system daemon - url: {}, action: {}",
                    screenEndpointUrl, screenAction
            );
        }
        catch (Exception e)
        {
            LOG.error("Failed to make system daemon request - url: {}, action: {}",
                    screenEndpointUrl, screenAction, e);
        }
    }

}
