package com.limpygnome.daemon.api.rest;

import com.limpygnome.daemon.util.StreamUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import org.json.simple.JSONObject;

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

    public void writeJsonResponse(JSONObject jsonObject) throws IOException
    {
        StreamUtil.writeJsonResponse(httpExchange, jsonObject);
    }

}
