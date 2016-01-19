package com.limpygnome.client.launcher.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Used to provide a web server for dashboard content.
 */
public class WebServerService implements Service
{
    private Server server;

    @Override
    public void start(Controller controller)
    {
        // Start the Jetty...
        try
        {
            server = new Server(1010);

            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setBaseResource(Resource.newResource("/"));
            resourceHandler.setDirectoriesListed(true);

            server.setHandler(resourceHandler);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to startup Jetty...");
        }
    }

    @Override
    public void stop(Controller controller)
    {
        // Kill the Jetty...
        server.stop();
        server.join();
    }

}
