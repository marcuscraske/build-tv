package com.limpygnome.daemon.api;

import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The facade/controller for a daemon, which acts as the bridge between all the internal services/components.
 */
public class Controller
{
    private final static String CONFIG_PATH_DEV = "deploy/files/config";
    private final static String CONFIG_PATH_PRODUCTION = "config";

    private static final Logger LOG = LogManager.getLogger(Controller.class);

    private String controllerName;
    private ControllerState state;
    private HashMap<String, Service> services;
    private Settings settings;

    public Controller(String controllerName)
    {
        this.controllerName = controllerName;
        this.state = ControllerState.STOPPED;
        this.services = new HashMap<>();
        this.settings = new Settings();
    }

    public synchronized void add(String serviceName, Service service)
    {
        // Check controller is not already running...
        if (state == ControllerState.RUNNING)
        {
            throw new RuntimeException("Cannot add service whilst running - name: " + serviceName +
                    ", class: " + service.getClass().getName()
            );
        }

        // Check service does not already exist
        if (services.containsKey(serviceName))
        {
            throw new RuntimeException("Service '" + serviceName + "' already exists");
        }

        // Add service
        services.put(serviceName, service);
    }

    public synchronized void start()
    {
        LOG.info("Starting controller...");

        setState(ControllerState.STARTING);

        // Reload settings
        settings.reload(this);

        // Start all services
        for (Map.Entry<String, Service> kv : services.entrySet())
        {
            LOG.debug("Starting service - name: {}", kv.getKey());

            kv.getValue().start(this);

            LOG.debug("Started service successfully - name: {}", kv.getKey());
        }

        setState(ControllerState.RUNNING);

        LOG.info("Controller started successfully");
    }

    public synchronized void stop()
    {
        LOG.info("Controller stopping...");

        setState(ControllerState.STOPPING);

        // Stop all services
        for (Map.Entry<String, Service> kv : services.entrySet())
        {
            LOG.debug("Stopping service - name: {}", kv.getKey());

            kv.getValue().stop(this);

            LOG.debug("Stopped service - name: {}", kv.getKey());
        }

        setState(ControllerState.STOPPED);

        LOG.info("Controller has stopped");
    }

    public synchronized void waitForExit()
    {
        waitForState(ControllerState.STOPPED);
    }

    public synchronized void waitForState(ControllerState state)
    {
        while (this.state.LIFECYCLE_STEP < state.LIFECYCLE_STEP)
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
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                Controller.this.stop();
            }
        });

        LOG.debug("Hooked runtime shutdown event");
    }

    public synchronized void hookAndStartAndWaitForExit()
    {
        hookShutdown();
        start();
        waitForExit();
    }

    /**
     * Attempts to find a config file.
     *
     * @param fileName the filename or relative path
     * @return found instance
     */
    public synchronized File findConfigFile(String fileName)
    {
        File file = getFilePathConfig(fileName);

        if (file.exists())
        {
            return file;
        }

        // Build exception of possible paths
        StringBuilder exMessage = new StringBuilder();
        exMessage.append("Unable to find config file - '" + fileName + "'; expected at:\n");
        exMessage.append(file.getAbsolutePath());

        throw new RuntimeException(exMessage.toString());
    }

    /**
     * Provides the path to a configuration file.
     *
     * @param fileName The name of the file at the base of the configuration path
     * @return The file instance to this file; may not exist
     */
    public synchronized File getFilePathConfig(String fileName)
    {
        if (EnvironmentUtil.isDevEnvironment())
        {
            return new File(CONFIG_PATH_DEV, fileName);
        }
        else
        {
            return new File(CONFIG_PATH_PRODUCTION, fileName);
        }
    }

    public String getControllerName()
    {
        return controllerName;
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

    private void setState(ControllerState state)
    {
        this.state = state;
        notifyAll();

        LOG.debug("Changed state of controller - state: {}", this.state);
    }

    public Settings getSettings()
    {
        return settings;
    }

    public synchronized Map<String, Service> getServices()
    {
        return new HashMap<>(services);
    }

}
