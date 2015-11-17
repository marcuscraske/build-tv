package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.rest.RestRequest;
import com.limpygnome.daemon.common.rest.RestResponse;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.remote.model.ComponentType;
import com.limpygnome.daemon.remote.service.auth.AuthTokenProviderService;
import com.limpygnome.daemon.util.RestClient;
import com.limpygnome.daemon.util.StreamUtil;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * A service to forward REST requests, based on their top-level path, to the appropriate daemon.
 *
 * This allows other daemons to listen on only the local interface, whilst this daemon proxies requests whilst
 * providing authentication, acting as a firewall.
 */
public class RestProxyService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(RestProxyService.class);

    public static final String SERVICE_NAME = "rest-proxy";

    /**
     * The user agent for proxied requests to other daemons.
     */
    private static final String USER_AGENT = "rest-proxy-service";

    /**
     * The buffer size for receiving data from other daemons.
     */
    private static final int BUFFER_SIZE = 4096;

    private AuthTokenProviderService authProviderService;
    private Map<ComponentType, String> daemonUrls;

    @Override
    public void start(Controller controller)
    {
        authProviderService = (AuthTokenProviderService) controller.getServiceByName("auth");

        // Build URLs for local daemons using settings
        daemonUrls = new HashMap<>();

        String url;
        for (ComponentType componentType : ComponentType.values())
        {
            url = "http://localhost:" + controller.getSettings().getLong(componentType.SETTING_KEY_PORT);
            daemonUrls.put(componentType, url);
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
            restResponse.sendStatusJson(restResponse, 403);
            return true;
        }

        // Fetch the top-level path to work out which daemon should receive the message
        String daemonPath = restRequest.getPathSegmentSafely(0);

        if (daemonPath != null)
        {
            // Match path to daemon type for forwarding
            for (ComponentType componentType : ComponentType.values())
            {
                if (componentType.TOP_LEVEL_PATH.equals(daemonPath))
                {
                    forward(componentType, restRequest, restResponse);
                    return true;
                }
            }
        }

        return false;
    }

    public void forward(ComponentType componentType, RestRequest restRequest, RestResponse restResponse)
    {
        try
        {
            // Build URL
            String url = daemonUrls.get(componentType) + restRequest.getPath();

            // Execute request
            RestClient restClient = new RestClient(USER_AGENT, BUFFER_SIZE);
            HttpResponse httpResponse = restClient.executePost(url, restRequest.getJsonRoot());

            // Check if the response can be parsed
            try
            {
                String response = StreamUtil.readInputStream(httpResponse.getEntity().getContent(), BUFFER_SIZE);

                // Send status header with size
                restResponse.sendStatus(httpResponse.getStatusLine().getStatusCode(), response.length());

                // Write response data (if available)
                if (response.length() > 0)
                {
                    restResponse.writeResponseIgnoreExceptions(restResponse, response);
                }
            }
            catch (Exception e)
            {
                LOG.error("Failed to construct daemon response - ip: {}, path: {}; daemon: {}",
                        restRequest.getHttpExchange().getRemoteAddress(),
                        restRequest.getPath(),
                        componentType.name(),
                        e
                );
            }
        }
        catch (Exception e)
        {
            LOG.error("Failed to forward request - ip: {}, path: {}, daemon: {}",
                    restRequest.getHttpExchange().getRemoteAddress(),
                    restRequest.getPath(),
                    componentType.name(),
                    e
            );

            restResponse.sendStatusJson(restResponse, 500);
        }
    }

}
