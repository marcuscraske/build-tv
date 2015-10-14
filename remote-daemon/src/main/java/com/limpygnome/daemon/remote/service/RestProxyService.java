package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.limpygnome.daemon.api.rest.RestResponse;
import com.limpygnome.daemon.api.rest.RestServiceHandler;
import com.limpygnome.daemon.remote.model.DaemonType;
import com.limpygnome.daemon.remote.service.auth.AuthProviderService;
import com.limpygnome.daemon.util.RestClient;
import com.limpygnome.daemon.util.StreamUtil;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Forwards REST requests, based on their top-level path, to the appropriate daemon.
 */
public class RestProxyService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(RestProxyService.class);

    /**
     * The user agent for proxied requests to other daemons.
     */
    private static final String USER_AGENT = "rest-proxy-service";

    /**
     * The buffer size for receiving data from other daemons.
     */
    private static final int BUFFER_SIZE = 4096;

    private AuthProviderService authProviderService;
    private Map<DaemonType, String> daemonUrls;

    @Override
    public void start(Controller controller)
    {
        authProviderService = (AuthProviderService) controller.getServiceByName("auth");

        // Build URLs for local daemons using settings
        daemonUrls = new HashMap<>();

        String url;
        for (DaemonType daemonType : DaemonType.values())
        {
            url = "http://localhost:" + controller.getSettings().getString(daemonType.SETTING_KEY_PORT);
            daemonUrls.put(daemonType, url);
        }
    }

    @Override
    public void stop(Controller controller)
    {
        authProviderService = null;

        daemonUrls.clear();
        daemonUrls = null;
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        // Since auth is required, requests will always be JSON
        if (!restRequest.isJsonRequest())
        {
            return false;
        }

        // Log the event
        LOG.info("New proxy request - path: {}, ip: {}", restRequest.getPath(), restRequest.getHttpExchange().getRemoteAddress());

        // Check request is authorised
        if (!authProviderService.isAuthorised(restRequest))
        {
            JSONObject response = new JSONObject();
            response.put("status", "403");
            restResponse.writeJsonResponseIgnoreExceptions(response);
        }

        // Fetch the top-level path to work out which daemon should receive the message
        String daemonPath = restRequest.getPathSegmentSafely(0);

        if (daemonPath != null)
        {
            // Match path to daemon type for forwarding
            for (DaemonType daemonType : DaemonType.values())
            {
                if (daemonType.TOP_LEVEL_PATH.equals(daemonPath))
                {
                    forward(daemonType, restRequest, restResponse);
                    return true;
                }
            }
        }

        return false;
    }

    public void forward(DaemonType daemonType, RestRequest restRequest, RestResponse restResponse)
    {
        try
        {
            // Build URL
            String url = daemonUrls.get(daemonType) + restRequest.getPath();

            // Execute request
            RestClient restClient = new RestClient(USER_AGENT, BUFFER_SIZE);
            HttpResponse httpResponse = restClient.executePost(url, restRequest.getJsonRoot());

            // Check if the response can be parsed
            try
            {
                String response = StreamUtil.readInputStream(httpResponse.getEntity().getContent(), BUFFER_SIZE);

                if (response.length() > 0)
                {
                    restResponse.writeResponseIgnoreExceptions(response);
                }
            }
            catch (Exception e)
            {
                LOG.error("Failed to construct daemon response - ip: {}, path: {}; daemon: {}",
                        restRequest.getHttpExchange().getRemoteAddress(),
                        restRequest.getPath(),
                        daemonType.name(),
                        e
                );
            }
        }
        catch (Exception e)
        {
            LOG.error("Failed to forward request - ip: {}, path: {}, daemon: {}",
                    restRequest.getHttpExchange().getRemoteAddress(),
                    restRequest.getPath(),
                    daemonType.name(),
                    e
            );
        }
    }

}