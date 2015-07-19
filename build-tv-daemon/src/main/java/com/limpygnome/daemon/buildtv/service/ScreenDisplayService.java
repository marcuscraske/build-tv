package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by limpygnome on 19/07/15.
 */
public class ScreenDisplayService implements Service
{
    private static final Logger LOG = LogManager.getLogger(ScreenDisplayService.class);

    private static final long ACTION_TIMEOUT_MS = 10000;

    private long lastAction;
    private boolean devMachine;
    private boolean screenOn;

    public ScreenDisplayService()
    {
        this.lastAction = 0;
        this.devMachine = false;
        this.screenOn = false;
    }

    @Override
    public synchronized void start(Controller controller)
    {
        // Check if running on dev machine
        // TODO: horrible hack...
        devMachine = new File("pom.xml").exists();

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

//            exec("xset dpms force on");
//            exec("xset -dpms");
//            exec("xset s off");
//            exec("xset s noblank");

            exec("/opt/vc/bin/tvservice -p");
            exec("fbset -accel true");

            this.screenOn = true;
        }
    }

    public synchronized void screenOff()
    {
        if (!isTooSoon() && this.screenOn)
        {
            LOG.debug("Turning screen off...");

//            exec("xset s reset");
//            exec("xset dpms force off");
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
            } catch (Exception e)
            {
                LOG.error("Exception executing command", e);
                LOG.error("Failed to execute command - cmd: {}", command);
            }
        }
    }

}
