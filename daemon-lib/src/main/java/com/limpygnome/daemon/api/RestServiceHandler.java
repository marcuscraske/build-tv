package com.limpygnome.daemon.api;

import com.sun.net.httpserver.HttpExchange;
import org.json.simple.JSONObject;

/**
 * Created by limpygnome on 20/07/15.
 */
public interface RestServiceHandler
{
    void start(Controller controller);

    void stop(Controller controller);

    boolean handleRequestInChain(HttpExchange httpExchange, JSONObject jsonRoot);
}
