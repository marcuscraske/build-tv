package com.limpygnome.daemon.api.rest;

import com.limpygnome.daemon.util.JsonUtil;
import com.limpygnome.daemon.util.StreamUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Used to parse and carry REST request data.
 */
public class RestRequest
{
    private HttpExchange httpExchange;
    private JSONObject jsonRoot;
    private String path;
    private String[] pathSegments;

    public RestRequest(HttpExchange httpExchange) throws IOException
    {
        this.httpExchange = httpExchange;

        // Parse request data
        String request = StreamUtil.readInputStream(httpExchange.getRequestBody(), 4096);

        // Attempt to parse as JSON
        try
        {
            // Attempt to parse as json
            if (request.length() > 0)
            {
                JSONParser jsonParser = new JSONParser();
                this.jsonRoot = (JSONObject) jsonParser.parse(request.toString());
            }
            else
            {
                // This is allowed...
                jsonRoot = null;
            }
        }
        catch (Exception e)
        {
            httpExchange.sendResponseHeaders(500, 0);

            throw new IOException("Unable to parse request as JSON - ip: " + httpExchange.getRemoteAddress(), e);
        }

        // Check path is valid
        URI requestUri = httpExchange.getRequestURI();

        if (requestUri == null)
        {
            throw new IOException("Invalid request path - ip: " + httpExchange.getRemoteAddress());
        }

        this.path = requestUri.getPath();

        if (path == null || path.length() == 0)
        {
            throw new IOException("Empty request path - ip: " + httpExchange.getRemoteAddress());
        }

        // Split path into segments divided by directory/level
        String pathToSegment = path;

        if (pathToSegment.startsWith("/") && pathToSegment.length() > 1)
        {
            pathToSegment = pathToSegment.substring(1);
        }

        this.pathSegments = pathToSegment.split("/");
    }

    public HttpExchange getHttpExchange()
    {
        return httpExchange;
    }

    public JSONObject getJsonRoot()
    {
        return jsonRoot;
    }

    public Object getJsonElement(String[] path)
    {
        return JsonUtil.getNestedNode(jsonRoot, path);
    }

    public boolean isJsonRequest()
    {
        return jsonRoot != null;
    }

    public String getPath()
    {
        return path;
    }

    public String[] getPathSegments()
    {
        return pathSegments;
    }

    /**
     * Safely retrieves a path segment
     *
     * @param index The index, or level, of the path segment to retrieve
     * @return Either the segment or null
     */
    public String getPathSegmentSafely(int index)
    {
        return index >= pathSegments.length ? null : pathSegments[index];
    }

}
