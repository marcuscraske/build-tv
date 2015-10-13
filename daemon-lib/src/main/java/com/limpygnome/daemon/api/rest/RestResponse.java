package com.limpygnome.daemon.api.rest;

import com.sun.net.httpserver.HttpExchange;

/**
 * Created by limpygnome on 13/10/15.
 */
public class RestResponse
{
    private HttpExchange httpExchange;

    public RestResponse(HttpExchange httpExchange)
    {
        this.httpExchange = httpExchange;
    }

    public HttpExchange getHttpExchange()
    {
        return httpExchange;
    }
}
