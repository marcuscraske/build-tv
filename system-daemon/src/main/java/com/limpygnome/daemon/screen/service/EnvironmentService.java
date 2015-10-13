package com.limpygnome.daemon.screen.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by limpygnome on 13/10/15.
 */
public class EnvironmentService implements Service
{
    private static final Logger LOG = LogManager.getLogger(EnvironmentService.class);

    @Override
    public void start(Controller controller) { }

    @Override
    public void stop(Controller controller) { }

    /**
     * Executes a command.
     *
     * @param command
     */
    public void exec(String command, long processTimeout)
    {
        if (EnvironmentUtil.isDevEnvironment())
        {
            LOG.debug("Mock executed command - command: {}" , command);
        }
        else
        {
            try
            {
                LOG.debug("Executing command - command: {}", command);

                // Run the command
                Process process = Runtime.getRuntime().exec(command);

                // Wait for process to finish, or kill it
                if (!process.waitFor(processTimeout, TimeUnit.MILLISECONDS))
                {
                    process.destroy();

                    LOG.warn("Forcibly killed process executing command - cmd: {}", command);
                }

            }
            catch (Exception e)
            {
                LOG.error("Exception executing command", e);
                LOG.error("Failed to execute command - command: {}", command);
            }
        }
    }

}
