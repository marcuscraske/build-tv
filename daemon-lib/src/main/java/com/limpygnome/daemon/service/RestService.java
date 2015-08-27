package com.limpygnome.daemon.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.util.StreamUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * A generic REST service used to control this daemon.
 *
 * Requests can be handled by multiple handlers, using chain pattern.
 */
public class RestService implements Service, HttpHandler
{
    private static final Logger LOG = LogManager.getLogger(RestService.class);

    private static final String SERVICE_NAME_SHARED_RUNTIME = "rest";

    private HttpServer httpServer;
    private final List<RestServiceHandler> restServiceHandlers;

    private RestService()
    {
        this.restServiceHandlers = new LinkedList<>();
    }

    @Override
    public synchronized void start(Controller controller)
    {
        // Start HTTP server
        try
        {
            int endpointPort = controller.getSettings().getInt("rest/port");

            httpServer = HttpServer.create(new InetSocketAddress(endpointPort), 16);

            httpServer.createContext("/", this);
            httpServer.setExecutor(null);
            httpServer.start();

            LOG.info("REST listening on port {}", endpointPort);
        }
        catch (Exception e)
        {
            LOG.error("Failed to start REST service", e);
        }

        // Inform handlers
        for (RestServiceHandler restServiceHandler : restServiceHandlers)
        {
            restServiceHandler.start(controller);
        }
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        // Inform handlers
        for (RestServiceHandler restServiceHandler : restServiceHandlers)
        {
            restServiceHandler.stop(controller);
        }

        // Stop HTTP server
        httpServer.stop(0);
        httpServer = null;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        LOG.debug(
                "Request received - ip: {}, path: {}",
                httpExchange.getRemoteAddress(),
                httpExchange.getRequestURI().getPath()
        );

        // Read raw request
        String request = StreamUtil.readInputStream(httpExchange.getRequestBody(), 4096);

        try
        {
            JSONObject jsonRoot;

            // Attempt to parse as json
            if (request.length() > 0)
            {
                JSONParser jsonParser = new JSONParser();
                jsonRoot = (JSONObject) jsonParser.parse(request.toString());
            }
            else
            {
                jsonRoot = null;
            }

            // Pass to handlers
            boolean handled = false;
            for (RestServiceHandler restServiceHandler : restServiceHandlers)
            {
                if (restServiceHandler.handleRequestInChain(httpExchange, jsonRoot))
                {
                    handled = true;
                    LOG.debug("REST request handled - handler: {}", restServiceHandler.getClass().getName());
                    break;
                }
            }

            if (!handled)
            {
                LOG.warn("Unhandled REST request - path: {}", httpExchange.getRequestURI().getPath());
                httpExchange.sendResponseHeaders(400, 0);
            }
            else
            {
                // We may have already sent the headers, from a rest handler, so this doesn't matter
                try
                {
                    httpExchange.sendResponseHeaders(200, 0);
                }
                catch (Exception e) { }
            }
        }
        catch (Exception e)
        {
            LOG.error("Failed to process web request", e);
            e.printStackTrace();

            httpExchange.sendResponseHeaders(500, 0);
        }

        // Dispose connection
        httpExchange.close();
    }

    public static void addRestHandlerToControllerRuntime(Controller controller, RestServiceHandler restServiceHandler)
    {
        RestService restService;

        // Fetch, else create, instance of this service
        try
        {
            restService = (RestService) controller.getServiceByName(SERVICE_NAME_SHARED_RUNTIME);
        }
        catch (Exception e)
        {

            LOG.debug("No existing REST service found, creating new instance");
            restService = new RestService();
            controller.add(SERVICE_NAME_SHARED_RUNTIME, restService);
        }

        // Add handler
        restService.restServiceHandlers.add(restServiceHandler);
    }

}
