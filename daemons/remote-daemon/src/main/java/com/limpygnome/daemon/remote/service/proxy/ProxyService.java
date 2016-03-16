package com.limpygnome.daemon.remote.service.proxy;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by limpygnome on 16/03/16.
 */
public class ProxyService implements Service
{
    private static final Logger LOG = LogManager.getLogger(ProxyService.class);

    private HttpServer httpServer;
    private ProxyServiceHandler handler;

    @Override
    public void start(Controller controller)
    {
        try
        {
            InetSocketAddress address = new InetSocketAddress(2100);
            httpServer = HttpServer.create(address, 123);
            handler = new ProxyServiceHandler();

            httpServer.createContext("/", handler);
            httpServer.setExecutor(null);
            httpServer.start();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to start proxy server socket");
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
        }
    }

}
