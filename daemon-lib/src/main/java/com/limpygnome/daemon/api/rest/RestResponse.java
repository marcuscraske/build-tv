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
    private boolean flagStatusSent;

    public RestResponse(HttpExchange httpExchange)
    {
        this.httpExchange = httpExchange;
        this.flagStatusSent = false;
    }

    public HttpExchange getHttpExchange()
    {
        return httpExchange;
    }

    public void writeJsonResponse(RestResponse restResponse, JSONObject jsonObject) throws IOException
    {
        StreamUtil.writeJsonResponse(restResponse, jsonObject);
    }

    public void writeJsonResponseIgnoreExceptions(RestResponse restResponse, JSONObject jsonObject)
    {
        try
        {
            writeJsonResponse(restResponse, jsonObject);
        }
        catch (Exception e)
        {
            LOG.debug("Failed to send JSON response - ip: {}", httpExchange.getRemoteAddress(), e);
        }
    }

    public void writeResponseIgnoreExceptions(RestResponse restResponse, String response)
    {
        try
        {
            StreamUtil.writeResponse(restResponse, response);
        }
        catch (Exception e)
        {
            LOG.debug("Failed to send response - ip: {}", httpExchange.getRemoteAddress(), e);
        }
    }

    public void sendStatus(int statusCode, int dataLength)
    {
        if (!flagStatusSent)
        {
            try
            {
                httpExchange.sendResponseHeaders(statusCode, dataLength);
            }
            catch (IOException e)
            {
                LOG.debug("Failed to sent response headers", e);
            }

            // Set flag to avoid this call again
            flagStatusSent = true;

            LOG.debug("Sent status code - ip: {}, status code: {}", httpExchange.getRemoteAddress(), statusCode);
        }
        else
        {
            LOG.debug(
                    "Avoided sending HTTP status code, status header already sent - ip: {}, status code: {}",
                    httpExchange.getRemoteAddress(),
                    statusCode
            );
        }
    }

    /**
     * Sends the HTTP status code provided as both the actual HTTP status code and a JSON object with the status
     * code wrapped in an element called <i>status</i>.
     *
     * @param statusCode The HTTP status code for the response
     */
    public void sendStatusJson(RestResponse restResponse, int statusCode)
    {
        if (!flagStatusSent)
        {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", statusCode);

            String response = jsonResponse.toJSONString();

            // Send actual HTTP status code
            sendStatus(statusCode, response.length());

            // Create JSON object with status code
            writeResponseIgnoreExceptions(restResponse, response);

            LOG.debug("Sent status code object - ip: {}, status code: {}", httpExchange.getRemoteAddress(), statusCode);
        }
        else
        {
            LOG.debug(
                    "Avoided sending status code object, status header already sent - ip: {}, status code: {}",
                    httpExchange.getRemoteAddress(),
                    statusCode
            );
        }
    }

    /**
     * Indicates if the response status has been sent.
     *
     * @return True = sent, false = not sent.
     */
    public boolean isStatusSent()
    {
        return flagStatusSent;
    }

}
