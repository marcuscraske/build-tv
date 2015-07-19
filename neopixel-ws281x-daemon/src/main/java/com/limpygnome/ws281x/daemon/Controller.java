package com.limpygnome.ws281x.daemon;

import com.limpygnome.ws281x.daemon.service.api.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by limpygnome on 18/07/15.
 */
public class Controller
{
    private static final Logger LOG = LogManager.getLogger(Controller.class);

    private boolean running;
    private HashMap<String, Service> services;

    public Controller()
    {
        this.services = new HashMap<>();
    }

    public synchronized void add(String serviceName, Service service)
    {
        if (running)
        {
            throw new RuntimeException("Cannot add service whilst running - name: " + serviceName +
                    ", class: " + service.getClass().getName()
            );
        }

        services.put(serviceName, service);
    }

    public synchronized void start()
    {
        LOG.debug("Starting controller...");

        // Start all services
        for (Map.Entry<String, Service> kv : services.entrySet())
        {
            LOG.debug("Starting service - name: {}", kv.getKey());

            kv.getValue().start(this);

            LOG.debug("Started service successfully - name: {}", kv.getKey());
        }

        setRunning(true);

        LOG.debug("Controller started successfully");
    }

    public synchronized void stop()
    {
        // Stop all services
        for (Map.Entry<String, Service> kv : services.entrySet())
        {
            LOG.debug("Stopping service - name: {}", kv.getKey());

            kv.getValue().stop(this);

            LOG.debug("Stopped service - name: {}", kv.getKey());
        }

        setRunning(false);
    }

    public synchronized void waitForExit()
    {
        // Block until we exit...
        while (!running)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    public synchronized Service getServiceByName(String serviceName)
    {
        return services.get(serviceName);
    }

    private void setRunning(boolean value)
    {
        this.running = value;
        notifyAll();

        LOG.debug("Changed running state of controller - running: {}", this.running);
    }
}
