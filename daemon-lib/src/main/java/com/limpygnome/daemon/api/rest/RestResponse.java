package com.limpygnome.daemon.api.rest;

import com.limpygnome.daemon.util.StreamUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * Created by limpygnome on 13/10/15.
 */
public class RestResponse
{
    private static final Logger LOG = LogManager.getLogger(RestResponse.class);

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

    public void writeJsonResponseIgnoreExceptions(JSONObject jsonObject)
    {
        try
        {
            writeJsonResponse(jsonObject);
        }
        catch (Exception e)
        {
            LOG.debug("Failed to send JSON response - ip: {}", httpExchange.getRemoteAddress(), e);
        }
    }

    public void writeResponseIgnoreExceptions(String response)
    {
        try
        {
            StreamUtil.writeResponse(httpExchange, response);
        }
        catch (Exception e)
        {
            LOG.debug("Failed to send response - ip: {}", httpExchange.getRemoteAddress(), e);
        }
    }

}
