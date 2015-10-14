package com.limpygnome.daemon.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.limpygnome.daemon.api.rest.RestResponse;
import com.limpygnome.daemon.api.rest.RestServiceHandler;
import com.limpygnome.daemon.api.Service;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        // Stop HTTP server
        httpServer.stop(0);
        httpServer = null;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        // Create request/response objects
        RestRequest restRequest;
        RestResponse restResponse = null;

        try
        {
            restRequest = new RestRequest(httpExchange);
            restResponse = new RestResponse(httpExchange);

            LOG.debug(
                    "Request received - ip: {}, path: {}",
                    httpExchange.getRemoteAddress(),
                    restRequest.getPath()
            );

            // Pass to REST handlers
            boolean handled = false;

            for (RestServiceHandler restServiceHandler : restServiceHandlers)
            {
                if (restServiceHandler.handleRequestInChain(restRequest, restResponse))
                {
                    handled = true;
                    LOG.debug("REST request handled - handler: {}, path: {}",
                            restServiceHandler.getClass().getName(), restRequest.getPath()
                    );
                    break;
                }
            }

            // Check request handled
            if (!handled)
            {
                LOG.warn("Unhandled REST request - ip: {}, path: {}", httpExchange.getRemoteAddress(), restRequest.getPath());
                restResponse.sendStatusJson(restResponse, 404);
            }
            else
            {
                // We may have already sent the headers, from a rest handler, so this doesn't matter if it fails
                restResponse.sendStatus(200, 0);
            }
        }
        catch (Exception e)
        {
            LOG.error("Failed to process web request", e);

            if (restResponse != null)
            {
                restResponse.sendStatusJson(restResponse, 500);
            }
        }
        finally
        {

            // Dispose connection
            httpExchange.close();
        }
    }

    public static void addRestHandlerToControllerRuntime(Controller controller, RestServiceHandler restServiceHandler)
    {
        RestService restService;

        // Fetch, else create, instance of this service
        try
        {
            restService = (RestService) controller.getServiceByName(SERVICE_NAME_SHARED_RUNTIME);
        }
        catch (RuntimeException e)
        {

            LOG.debug("No existing REST service found, creating new instance");

            restService = new RestService();
            controller.add(SERVICE_NAME_SHARED_RUNTIME, restService);
        }

        // Add handler
        restService.restServiceHandlers.add(restServiceHandler);

        LOG.debug("REST handler added - " + restServiceHandler.getClass().getName());
    }

    /**
     * Attaches any REST handler capable services, added to the specified controller, to the current
     * controller's REST service.
     *
     * @param controller The current controller
     */
    public static void attachControllerRestHandlerServices(Controller controller)
    {
        Map<String, Service> services = controller.getServices();

        Service service;
        for (Map.Entry<String, Service> kv : services.entrySet())
        {
            service = kv.getValue();

            if (service instanceof RestServiceHandler)
            {
                addRestHandlerToControllerRuntime(controller, (RestServiceHandler) service);
            }
        }
    }

}
