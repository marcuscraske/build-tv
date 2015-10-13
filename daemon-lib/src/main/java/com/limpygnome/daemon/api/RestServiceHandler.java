package com.limpygnome.daemon.api;

import com.sun.net.httpserver.HttpExchange;
import org.json.simple.JSONObject;

/**
 * An interface to implement a REST service handler.
 *
 * This allows multiple handlers to sit on the same port, using the chain pattern to handle the request. All of the
 * heavy lifting is done internally.
 */
public interface RestServiceHandler
{

    /**
     * Invoked when the handler is started.
     *
     * @param controller The controller to which this handler belongs
     */
    void start(Controller controller);

    /**
     * Invoked when the handler is stopped.
     *
     * @param controller The controller to which this handler belongs
     */
    void stop(Controller controller);

    /**
     * Handles an incoming REST request using the chain pattern.
     *
     * @param httpExchange The internal HTTP exchange object for request and response data
     * @param jsonRoot The root element of the parsed JSON
     * @param path The relative path of the request e.g. <i>/</i> or <i>/rest</i>
     * @return True if handled, false if other rest handlers should handle this request
     */
    boolean handleRequestInChain(HttpExchange httpExchange, JSONObject jsonRoot, String path);

}
