package com.limpygnome.daemon.remote.service.auth;

import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.rest.RestRequest;

/**
 * An interface for an auth provider, which authenticates a REST handler request.
 */
public interface AuthTokenProviderService extends Service
{

    /**
     * All implementations of this service should use this constant as the service name.
     */
    String SERVICE_NAME = "auth";

    /**
     * Determines if a request is authorised.
     *
     * @param restRequest The REST request to authorise
     * @return True = authorised, false = not authorised
     */
    boolean isAuthorised(RestRequest restRequest);

    /**
     * Retrieves the auth token currently used for authentication.
     *
     * @return The current auth token
     */
    String getAuthToken();

}
