package com.limpygnome.daemon.remote.service.proxy;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * A service used to proxy web requests, with the intention of stripping headers. This is useful for sites attempting to
 * prevent embedding in an iframe, through headers such as x-frame etc.
 */
public class ProxyService implements Service
{
    private static final Logger LOG = LogManager.getLogger(ProxyService.class);

    public static final String SERVICE_NAME = "proxy";

    private static final int SOCKET_BACKLOG = 16;

    private HttpServer httpServer;
    private ProxyServiceHandler handler;

    @Override
    public void start(Controller controller)
    {
        try
        {
            // Read settings
            int proxyPort = controller.getSettings().getInt("proxy/port");

            // Startup basic web server to accept proxy requests
            InetSocketAddress address = new InetSocketAddress(proxyPort);
            httpServer = HttpServer.create(address, SOCKET_BACKLOG);
            handler = new ProxyServiceHandler(controller);

            httpServer.createContext("/", handler);
            httpServer.setExecutor(null);
            httpServer.start();

            LOG.info("Started proxy server");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to start proxy server socket", e);
        }
    }

    @Override
    public void stop(Controller controller)
    {
        if (httpServer != null)
        {
            httpServer.stop(0);
            httpServer = null;
            handler = null;

            LOG.info("Stopped proxy server");
        }
    }

}
