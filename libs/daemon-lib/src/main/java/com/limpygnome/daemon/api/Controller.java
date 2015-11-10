package com.limpygnome.daemon.api;

import com.limpygnome.daemon.common.Settings;
import java.io.File;
import java.util.Map;

/**
 * The facade/controller for a daemon, which acts as the bridge between all the internal services/components.
 */
public interface Controller
{

    /**
     * Adds a service to the controller.
     *
     * This can only be done whilst the controller is not running.
     *
     * @param serviceName The name for the service
     * @param service The service instance
     */
    void add(String serviceName, Service service);

    /**
     * Starts the controller.
     */
    void start();

    /**
     * Stops the controller.
     *
     * A service should not invoke this method if the service its self uses synchronization, else this will result in
     * a dead-lock.
     */
    void stop();

    /**
     * Hangs the invoking thread until the controller stops.
     */
    void waitForExit();

    /**
     * Hangs the invoking thread until a lifecycle state equal or greater than the specified state occurs. Greater
     * in the sense of a lifecycle state occuring later than the specified state.
     *
     * @param state The desired state
     */
    void waitForState(ControllerState state);

    /**
     * Hangs the invoking thread until the controller stops.
     */
    void hookShutdown();

    /**
     * Hooks the JVM shutdown/exit event to terminate the controller and hangs the invoking thread until the
     * controller stops.
     */
    void hookAndStartAndWaitForExit();

    /**
     * Attempts to find a config file.
     *
     * @param fileName the filename or relative path
     * @return found instance
     */
    File findConfigFile(String fileName);

    /**
     * Provides the path to a configuration file.
     *
     * @param fileName The name of the file at the base of the configuration path
     * @return The file instance to this file; may not exist
     */
    File getFilePathConfig(String fileName);

    /**
     * Retrieves the name of this controller.
     *
     * @return The name of this controller
     */
    String getControllerName();

    /**
     * Retrieves a service by name.
     *
     * @param serviceName The name of the service
     * @return An instance of the service
     * @throws RuntimeException If the service is not available
     */
    Service getServiceByName(String serviceName);

    /**
     * Retrieves global settings for this daemon.
     *
     * @return Settings instance
     */
    Settings getSettings();

    /**
     * Indicates if a daemon is available on the system.
     *
     * @param daemonName The name of the daemon
     * @return True = available, false = not present/not available
     */
    boolean isDaemonEnabled(String daemonName);

    /**
     * Retrieves a cloned map of available services.
     *
     * @return Cloned map
     */
    Map<String, Service> getServices();

}
