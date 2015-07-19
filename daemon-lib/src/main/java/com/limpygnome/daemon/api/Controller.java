package com.limpygnome.daemon.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The facade/controller for a daemon, which acts as the bridge between all the internal services/components.
 */
public class Controller
{
    private static final Logger LOG = LogManager.getLogger(Controller.class);

    private static final String SETTINGS_PATH = "config/daemon-settings.kv";

    private boolean running;
    private HashMap<String, Service> services;
    private HashMap<String, String> settings;

    public Controller()
    {
        this.services = new HashMap<>();
        this.settings = new HashMap<>();
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
        LOG.info("Starting controller...");

        reloadSettings();

        // Start all services
        for (Map.Entry<String, Service> kv : services.entrySet())
        {
            LOG.debug("Starting service - name: {}", kv.getKey());

            kv.getValue().start(this);

            LOG.debug("Started service successfully - name: {}", kv.getKey());
        }

        setRunning(true);

        LOG.info("Controller started successfully");
    }

    public synchronized void stop()
    {
        LOG.info("Controller stopping...");

        // Stop all services
        for (Map.Entry<String, Service> kv : services.entrySet())
        {
            LOG.debug("Stopping service - name: {}", kv.getKey());

            kv.getValue().stop(this);

            LOG.debug("Stopped service - name: {}", kv.getKey());
        }

        setRunning(false);

        LOG.info("Controller has stopped");
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

    public synchronized void hookShutdown()
    {
        // This will hook an event to stop the controller when the JVM is shutting down
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
                Controller.this.stop();
            }
        });

        LOG.debug("Hooked runtime shutdown event");
    }

    public synchronized Service getServiceByName(String serviceName)
    {
        Service service = services.get(serviceName);

        if (service == null)
        {
            throw new RuntimeException("Service '" + serviceName + "' missing");
        }

        return service;
    }

    public synchronized String getSetting(String key)
    {
        String value = settings.get(key);

        if (value == null)
        {
            throw new RuntimeException("Setting '" + key + "' missing");
        }

        return value;
    }

    public synchronized long getSettingLong(String key)
    {
        String value = getSetting(key);

        try
        {
            return Long.parseLong(value);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Malformed setting '" + key + "', expected value to be of type long", e);
        }
    }

    public synchronized int getSettingInt(String key)
    {
        String value = getSetting(key);

        try
        {
            return Integer.parseInt(value);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Malformed setting '" + key + "', expected value to be of type int", e);
        }
    }

    public synchronized boolean getSettingBoolean(String key)
    {
        String value = getSetting(key);

        try
        {
            return Boolean.parseBoolean(value);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Malformed setting '" + key + "', expected value to be of type bool", e);
        }
    }


    private void reloadSettings()
    {
        settings.clear();

        LOG.debug("Reloading settings...");

        try
        {
            // Read file line-by-line
            BufferedReader bufferedReader = new BufferedReader(new FileReader(SETTINGS_PATH));
            String line, key, value;
            int splitIndex;

            while ((line = bufferedReader.readLine()) != null)
            {
                line = line.trim();

                // Check line isnt empty or comment
                if (!line.startsWith("#") && line.length() != 0)
                {
                    splitIndex = line.indexOf('=');

                    if (splitIndex > 1 && splitIndex < line.length() - 1)
                    {
                        // Read KV
                        key = line.substring(0, splitIndex);
                        value = line.substring(splitIndex+1);

                        // Add KV
                        settings.put(key, value);
                        LOG.debug("Added setting - key: {}, value: {}", key, value);
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOG.error("Unable to load settings", e);
        }

        LOG.debug("Finished loading settings");
    }

    private void setRunning(boolean value)
    {
        this.running = value;
        notifyAll();

        LOG.debug("Changed running state of controller - running: {}", this.running);
    }
}
