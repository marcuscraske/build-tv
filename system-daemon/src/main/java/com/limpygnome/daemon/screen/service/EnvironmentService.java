package com.limpygnome.daemon.screen.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by limpygnome on 13/10/15.
 */
public class EnvironmentService implements Service
{
    public static final String SERVICE_NAME = "environment";

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

    /**
     * Attempts to execute the command and return a float.
     *
     * If the output is not valid, or an exception occurs, null is returned.
     *
     * @param command The command to execute
     * @param processTimeout
     * @return The value of the command, or null if invalid
     */
    public Float execute(String command, long processTimeout)
    {
        final long BUFFER_LIMIT = 512;

        Process process = null;

        try
        {
            // Run the process
            process = Runtime.getRuntime().exec(command);

            // Hook into stdout
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // Read first available line
            StringBuilder buffer = new StringBuilder();
            long start = System.currentTimeMillis();
            String line;

            do
            {
                line = bufferedReader.readLine();

                if (line != null)
                {
                    buffer.append(line);
                }
            }
            while (process.isAlive() && System.currentTimeMillis() - start < processTimeout && buffer.length() < BUFFER_LIMIT);

            // Attempt to parse output
            if (buffer.length() > 0)
            {
                try
                {
                    return Float.parseFloat(buffer.toString());
                }
                catch (NumberFormatException e)
                {
                    LOG.warn("Failed to parse command output - command: {}", command, e);
                }
            }
        }
        catch (Exception e)
        {
            LOG.error("Failed to run process for stats", e);
        }
        finally
        {
            if (process != null)
            {
                try
                {
                    if (process.isAlive())
                    {
                        process.destroy();
                    }
                }
                catch (Exception e2)
                {
                    LOG.error("Failed to destroy stats process", e2);
                }
            }
        }

        return null;
    }

}
