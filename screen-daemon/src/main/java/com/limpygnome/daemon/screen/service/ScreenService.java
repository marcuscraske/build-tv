package com.limpygnome.daemon.screen.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * A service used to control the attached screen.
 */
public class ScreenService implements Service
{
    private static final Logger LOG = LogManager.getLogger(ScreenService.class);

    /**
     * The timeout between requests. Any requests during the timeout period are ignored. It's expected that
     * external services will periodically call this daemon, irregardless of the screen state changing. THis is to
     * allow the daemon to restart or for the external screen to get out of sync.
     */
    private static final long ACTION_TIMEOUT_MS = 10000;

    private long lastAction;
    private boolean devMachine;
    private boolean screenOn;

    public ScreenService()
    {
        this.lastAction = 0;
        this.devMachine = false;
        this.screenOn = false;
    }

    @Override
    public synchronized void start(Controller controller)
    {
        // Check if running on dev machine
        devMachine = EnvironmentUtil.isDevEnvironment();

        if (devMachine)
        {
            LOG.warn("Screen service stubbed, dev machine detected...");
        }

        // Make sure the screen is initially on
        screenOn();
    }

    @Override
    public synchronized void stop(Controller controller)
    {
    }

    public synchronized void screenOn()
    {
        if (!isTooSoon() && !this.screenOn)
        {
            LOG.debug("Turning screen on...");

            exec("/opt/vc/bin/tvservice -p");
            exec("fbset -depth 0");
            exec("fbset -depth 16");
            exec("fbset -accel true");

            this.screenOn = true;
        }
    }

    public synchronized void screenOff()
    {
        if (!isTooSoon() && this.screenOn)
        {
            LOG.debug("Turning screen off...");

            exec("/opt/vc/bin/tvservice -o");

            this.screenOn = false;
        }
    }

    private boolean isTooSoon()
    {
        long currTime = System.currentTimeMillis();

        if (currTime - lastAction > ACTION_TIMEOUT_MS)
        {
            lastAction = currTime;
            return false;
        }

        LOG.debug("Cannot perform screen operation, timeout in effect...");
        return true;
    }

    private void exec(String command)
    {
        final long PROCESS_TIMEOUT = 5000;

        if (devMachine)
        {
            LOG.debug("Mock executed command - cmd: {}" , command);
        }
        else
        {
            try
            {
                LOG.debug("Executing command - cmd: {}", command);

                // Run the command
                Process process = Runtime.getRuntime().exec(command);

                // Wait for process to finish, or kill it
                if (!process.waitFor(PROCESS_TIMEOUT, TimeUnit.MILLISECONDS))
                {
                    process.destroy();

                    LOG.warn("Forcibly killed process executing command - cmd: {}", command);
                }

                // Give enough wait for the command to take affect
                // -- Hacky, but a new attempt at getting this to work through a daemon
                try
                {
                    Thread.sleep(4000);
                }
                catch (InterruptedException e) { }

            } catch (Exception e)
            {
                LOG.error("Exception executing command", e);
                LOG.error("Failed to execute command - cmd: {}", command);
            }
        }
    }

}
