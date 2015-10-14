package com.limpygnome.daemon.remote.service.auth;

import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.sun.net.httpserver.HttpExchange;
import org.json.simple.JSONObject;

/**
 * An interface for an auth provider, which authenticates a REST handler request.
 */
public interface AuthProviderService extends Service
{

    /**
     * Determines if a request is authorised.
     *
     * @param restRequest The REST request to authorise
     * @return True = authorised, false = not authorised
     */
    boolean isAuthorised(RestRequest restRequest);

}
