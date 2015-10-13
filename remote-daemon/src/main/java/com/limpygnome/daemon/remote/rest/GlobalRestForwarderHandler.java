package com.limpygnome.daemon.remote.rest;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.remote.service.RestForwarderService;
import com.limpygnome.daemon.remote.service.auth.AuthProviderService;
import com.limpygnome.daemon.util.StreamUtil;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * Created by limpygnome on 13/10/15.
 */
public class GlobalRestForwarderHandler implements RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(GlobalRestForwarderHandler.class);

    private AuthProviderService authProviderService;
    private RestForwarderService restForwarderService;

    @Override
    public void start(Controller controller)
    {
        // Fetch current auth provider
        authProviderService = (AuthProviderService) controller.getServiceByName("auth");
        restForwarderService = (RestForwarderService) controller.getServiceByName("forwarder");
    }

    @Override
    public void stop(Controller controller)
    {
        authProviderService = null;
    }

    @Override
    public boolean handleRequestInChain(HttpExchange httpExchange, JSONObject jsonRoot, String path)
    {
        try
        {
            // Log the event
            LOG.info("New request - path: {}, ip: {}", path, httpExchange.getRemoteAddress());

            // Check request is authorised
            if (!authProviderService.isAuthorised(httpExchange, jsonRoot, path))
            {
                JSONObject response = new JSONObject();
                response.put("status", "403");
                StreamUtil.writeJsonResponse(httpExchange, response);
            }

            // Forward requests to other daemons (acting as a secure proxy), by pulling start path
            String initialPath = getTopLevelPath(path);

            // Handle the request based on path
            switch (initialPath)
            {
                case "system":

                    break;
                case "screen":
                    break;
                case "build":
                    break;
                case "leds":
                    break;
                default:
                    JSONObject response = new JSONObject();
                    response.put("status", "404");
                    StreamUtil.writeJsonResponse(httpExchange, response);
                    break;
            }

            return false;
        }
        catch (Exception e)
        {
            LOG.error("Failed to handle request - ip: {}, path: {}", httpExchange.getRemoteAddress(), path, e);
            return false;
        }
    }

    private String getTopLevelPath(String path)
    {
        // Check path is valid
        if (path == null || path.length() == 0)
        {
            return "";
        }

        // Find first instance of / not at index 0
        int separatorIndex = path.indexOf('/', 1);

        if (separatorIndex <= 0)
        {
            return "";
        }

        // Find the next /
        int separatorIndexFollowing = path.indexOf('/', separatorIndex+1);

        if (separatorIndexFollowing == -1)
        {
            return path.substring(separatorIndex+1);
        }
        else
        {
            return path.substring(separatorIndex+1, separatorIndexFollowing);
        }
    }

}
