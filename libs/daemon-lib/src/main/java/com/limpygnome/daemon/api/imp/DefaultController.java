package com.limpygnome.daemon.api.imp;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ControllerState;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.Settings;
import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of a controller.
 */
public class DefaultController implements Controller
{
    private static final Logger LOG = LogManager.getLogger(DefaultController.class);

    /*
        Relative path to config in a development environment.
     */
    private final static String CONFIG_PATH_DEV = "deploy/files/config";

    /*
        Relative path to config in a production/live system.
     */
    private final static String CONFIG_PATH_PRODUCTION = "config";

    /*
        The global settings file used to check if a daemon is available on the system.
     */
    private static final String GLOBAL_SETTINGS_DAEMONS_ENABLED = "daemons-enabled.json";


    private String controllerName;
    private ControllerState state;
    private HashMap<String, Service> services;
    private Settings settings;
    private Settings daemonsAvailable;


    /**
     * Creates a new instance.
     *
     * @param controllerName The name of the controller, used for building path to global daemon settings file
     */
    public DefaultController(String controllerName)
    {
        this.controllerName = controllerName;
        this.state = ControllerState.STOPPED;
        this.services = new HashMap<>();
        this.settings = new Settings();
        this.daemonsAvailable = null;
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

        // Reload daemons available
        daemonsAvailable = new Settings();
        daemonsAvailable.reload(findConfigFile(GLOBAL_SETTINGS_DAEMONS_ENABLED));

        // Check daemon is enabled...
        if (!isDaemonEnabled(controllerName))
        {
            LOG.error("Daemon is not enabled, stopped...");
            setState(ControllerState.STOPPED);
            return;
        }

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
        // Check controller can be stopped...
        if (state == ControllerState.STOPPED)
        {
            return;
        }
        else if (state != ControllerState.RUNNING)
        {
            LOG.warn("Controller is not running, cannot stop...");
            return;
        }

        // Update state to stopping...
        LOG.info("Controller stopping...");

        setState(ControllerState.STOPPING);

        // Stop all services
        for (Map.Entry<String, Service> kv : services.entrySet())
        {
            LOG.debug("Stopping service - name: {}", kv.getKey());

            kv.getValue().stop(this);

            LOG.debug("Stopped service - name: {}", kv.getKey());
        }

        // Reset daemons available
        daemonsAvailable = null;

        // Update state
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
                DefaultController.this.stop();
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

    public boolean isDaemonEnabled(String daemonName)
    {
        try
        {
            return daemonsAvailable.getBoolean(daemonName);
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("Daemon '" + daemonName + "' is not present in daemons available file", e);
        }
    }

    public synchronized Map<String, Service> getServices()
    {
        return new HashMap<>(services);
    }

}
