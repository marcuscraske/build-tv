package com.limpygnome.client.launcher.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Used to provide a web server for dashboards content.
 */
public class WebServerService implements Service
{
    /* The unique name for this service. */
    public static final String SERVICE_NAME = "webserver-jetty";

    /* The context path at which resources are handled/served. */
    private static final String CONTEXT_PATH = "/";

    /* The class-path to resources within this application's artifact.  */
    private static final String RESOURCE_PATH = "/website";

    /* Jetty embedded web server. */
    private Server server;

    /* Cached construction of the URL of where the website can be accessed locally. */
    private String webserverUrl;

    @Override
    public void start(Controller controller)
    {
        try
        {
            int webserverPort = controller.getSettings().getInt("webserver/port");

            // Build webserver URL
            webserverUrl = "http://localhost:" + webserverPort;

            // Start the Jetty...
            server = new Server();

            // -- Setup connector for localhost listening (important for security)
            ServerConnector connector = new ServerConnector(server);
            connector.setHost("localhost");
            connector.setPort(webserverPort);
            server.addConnector(connector);

            // -- Setup resource handler to serve assets from class-path
            WebServerHandler webServerHandler = new WebServerHandler();
            webServerHandler.setBaseResource(Resource.newClassPathResource(RESOURCE_PATH));
            webServerHandler.setDirectoriesListed(true);
            webServerHandler.setWelcomeFiles(new String[]{"index.html"});

            // -- Setup context path for serving assets
            ContextHandler contextHandler = new ContextHandler(CONTEXT_PATH);
            contextHandler.setHandler(webServerHandler);

            server.setHandler(contextHandler);

            // -- Start the Jetty!
            server.start();
        }
        // Forced to catch Exception, ugly...
        catch (Exception e)
        {
            throw new RuntimeException("Failed to startup Jetty...", e);
        }
    }

    @Override
    public void stop(Controller controller)
    {
        try
        {
            // Kill the Jetty...
            if (server != null)
            {
                server.stop();
                server.join();
            }
        }
        // Jetty forces us to catch Exception, very ugly...
        catch (Exception e)
        {
            throw new RuntimeException("Failed to stop Jetty embedded webserver", e);
        }
    }

    /**
     * @return The URL at which the web server can be accessed
     */
    public String getWebserverUrl()
    {
        return webserverUrl;
    }

}
