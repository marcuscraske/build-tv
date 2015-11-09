package com.limpygnome.daemon.api.rest;

/**
 * An interface to implement a REST service handler.
 *
 * This allows multiple handlers to sit on the same port, using the chain pattern to handle the request. All of the
 * heavy lifting is done internally.
 */
public interface RestServiceHandler
{

    /**
     * Handles an incoming REST request using the chain pattern.
     *
     * @param restRequest The REST request data
     * @param restResponse The Rest response data
     * @return True if handled, false if other rest handlers should handle this request
     */
    boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse);

}
