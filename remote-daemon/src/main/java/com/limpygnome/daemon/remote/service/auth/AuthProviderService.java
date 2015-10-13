package com.limpygnome.daemon.remote.service.auth;

import com.limpygnome.daemon.api.Service;
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
     * @param httpExchange Represents the HTTP exchange
     * @param jsonRoot The JSON object parsed
     * @param path The relative path of the request
     * @return True = authorised, false = not authorised
     */
    boolean isAuthorised(HttpExchange httpExchange, JSONObject jsonRoot, String path);

}
