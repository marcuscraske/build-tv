package com.limpygnome.daemon.ws281x.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * A REST service used to safely control this daemon.
 */
public class RestService implements Service, HttpHandler
{
    private static final Logger LOG = LogManager.getLogger(RestService.class);

    private HttpServer httpServer;
    private LedService ledService;

    @Override
    public synchronized void start(Controller controller)
    {
        try
        {
            final int PORT = 2500;

            // Fetch instance of LED service
            ledService = (LedService) controller.getServiceByName("leds");

            // Start HTTP server
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 16);

            httpServer.createContext("/", this);
            httpServer.setExecutor(null);
            httpServer.start();

            LOG.info("REST listening on port {}", PORT);
        }
        catch (Exception e)
        {
            LOG.error("Failed to start REST service", e);
        }
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        httpServer.stop(0);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        LOG.debug("Request received - ip: {}", httpExchange.getRemoteAddress());

        // Read raw request
        StringBuilder requestBuffer = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));

        int readChars;
        char[] buffer = new char[1024];

        // There is a limit of 4096 bytes per request, which is beyond excessive!
        while ((readChars = bufferedReader.read(buffer)) > 0 && requestBuffer.length() < 4096)
        {
            requestBuffer.append(buffer, 0, readChars);
        }
        bufferedReader.close();

        try
        {
            if (requestBuffer.length() != 0)
            {
                // Attempt to parse as json
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonRoot = (JSONObject) jsonParser.parse(requestBuffer.toString());

                // Handle request elsewhere
                handleRequest(jsonRoot);
            }
        }
        catch (Exception e)
        {
            LOG.error("Failed to process web request", e);
            e.printStackTrace();
        }

        // Send response - always the same blank response
        httpExchange.sendResponseHeaders(200, 0);
        httpExchange.close();
    }

    public void handleRequest(JSONObject jsonRoot)
    {
        // Read, clean and validate requested LED pattern
        String pattern = (String) jsonRoot.get("pattern");

        pattern = pattern.trim().toLowerCase();

        if (!pattern.matches("^[a-z0-9\\_\\-]+$"))
        {
            throw new IllegalArgumentException("Invalid pattern specified in web request");
        }

        // Hand to LED service to use pattern
        if (ledService != null)
        {
            LOG.debug("Attempting pattern change - pattern: {}", pattern);

            ledService.setPattern(pattern);
        }
        else
        {
            LOG.warn("Unable to set LED pattern, LED service not available - pattern: {}", pattern);
        }
    }
}
