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
    private static final Logger LOG = LogManager.getLogger(Controller.class);

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
    public Controller(String controllerName)
    {
        this.controllerName = controllerName;
        this.state = ControllerState.STOPPED;
        this.services = new HashMap<>();
        this.settings = new Settings();
        this.daemonsAvailable = null;
    }

    /**
     * Adds a service to the controller.
     *
     * This can only be done whilst the controller is not running.
     *
     * @param serviceName The name for the service
     * @param service The service instance
     */
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

    /**
     * Starts the controller.
     */
    public synchronized void start()
    {
        LOG.info("Starting controller...");

        setState(ControllerState.STARTING);

        // Reload settings
        settings.reload(this);

        // Reload daemons available
        daemonsAvailable = new Settings();
        daemonsAvailable.reload(findConfigFile(GLOBAL_SETTINGS_DAEMONS_ENABLED));

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

    /**
     * Stops the controller.
     *
     * A service should not invoke this method if the service its self uses synchronization, else this will result in
     * a dead-lock.
     */
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

        // Reset daemons available
        daemonsAvailable = null;

        // Update state
        setState(ControllerState.STOPPED);

        LOG.info("Controller has stopped");
    }

    /**
     * Hangs the invoking thread until the controller stops.
     */
    public synchronized void waitForExit()
    {
        waitForState(ControllerState.STOPPED);
    }

    /**
     * Hangs the invoking thread until a lifecycle state equal or greater than the specified state occurs. Greater
     * in the sense of a lifecycle state occuring later than the specified state.
     *
     * @param state The desired state
     */
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

    /**
     * Hangs the invoking thread until the controller stops.
     */
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

    /**
     * Hooks the JVM shutdown/exit event to terminate the controller and hangs the invoking thread until the
     * controller stops.
     */
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

    /**
     * Retrieves the name of this controller.
     *
     * @return The name of this controller
     */
    public String getControllerName()
    {
        return controllerName;
    }

    /**
     * Retrieves a service by name.
     *
     * @param serviceName The name of the service
     * @return An instance of the service
     * @throws RuntimeException If the service is not available
     */
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

    /**
     * Retrieves global settings for this daemon.
     *
     * @return Settings instance
     */
    public Settings getSettings()
    {
        return settings;
    }

    /**
     * Indicates if a daemon is available on the system.
     *
     * @param daemonName The name of the daemon
     * @return True = available, false = not present/not available
     */
    public boolean isDaemonEnabled(String daemonName)
    {
        return daemonsAvailable.getBoolean(daemonName);
    }

    /**
     * Retrieves a cloned map of available services.
     *
     * @return Cloned map
     */
    public synchronized Map<String, Service> getServices()
    {
        return new HashMap<>(services);
    }

}
