package com.limpygnome.daemon.util;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Commonly used functionality for interacting with the host environment.
 */
public class EnvironmentUtil
{
    private static final Logger LOG = LogManager.getLogger(EnvironmentUtil.class);

    private static final boolean DEV_ENVIRONMENT;

    static
    {
        // Determine if this is a dev environment and cache the result
        DEV_ENVIRONMENT = new File("pom.xml").exists();

        if (DEV_ENVIRONMENT)
        {
            LOG.warn("Development environment detected, flag set to true...");
        }
    }

    /**
     * Useful for hardware stubbing for local dev testing.
     *
     * @return
     */
    public static boolean isDevEnvironment()
    {
        return DEV_ENVIRONMENT;
    }

    /**
     * Retrieves the hostname.
     *
     * This will first attempt to read the hostname file (Linux), else fallback to using the hostname of the local
     * adapter (which may return localhost sometimes).
     *
     * If no hostname can be determined, "unknown" is returned.
     *
     * @return The hostname
     */
    public static String getHostname()
    {
        String hostname = null;

        // Attempt to access /etc/hostname
        try
        {
            File file = new File("/etc/hostname");

            if (file.exists() && file.canRead())
            {
                // Read first line of hostname file
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                hostname = bufferedReader.readLine();
                bufferedReader.close();
            }
        }
        catch (Exception e)
        {
            // Bad but we don't care about the exception...
        }

        if (hostname == null || hostname.length() == 0)
        {
            // Use network adapter as fallback
            try
            {
                return InetAddress.getLocalHost().getHostName();
            }
            catch (Exception e)
            {
                // Again, we don't care...
            }
        }

        return hostname != null && hostname.length() > 0 ? hostname : "unknown";
    }

    /**
     * Retrieves the IP address/hostname of the current machine.
     *
     * @return The IP address/hostname, or null
     */
    public static String getIpAddress()
    {
        try
        {
            String resultIpAddress = null;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            Enumeration<InetAddress> addresses;
            String addressStr;

            while (networkInterfaces.hasMoreElements())
            {
                addresses = networkInterfaces.nextElement().getInetAddresses();

                while (addresses.hasMoreElements())
                {
                    addressStr = addresses.nextElement().getHostAddress();

                    // Ignore local and give preference to ipv4
                    if (!addressStr.startsWith("127.") && !addressStr.contains("local") &&
                            !addressStr.startsWith(":") &&
                            (resultIpAddress == null || resultIpAddress.contains(":")))
                    {
                        resultIpAddress = addressStr;
                    }
                }
            }

            return resultIpAddress != null ? resultIpAddress : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Executes a command.
     *
     * @param commands The commands to execute
     */
    public static Process exec(String[] commands, long processTimeout, boolean mockingEnabled)
    {
        String commandsStr = StringUtils.join(commands, ",");

        if (mockingEnabled && isDevEnvironment())
        {
            LOG.debug("Mock executed command - commands: {}", commandsStr);
        }
        else
        {
            try
            {
                LOG.debug("Executing command - command: {}", commandsStr);

                // Run the command
                Process process = Runtime.getRuntime().exec(commands);

                // Wait for process to finish, or kill it
                long start = System.currentTimeMillis();

                if (processTimeout > 0)
                {
                    while (process.isAlive() && ((System.currentTimeMillis() - start) < processTimeout))
                    {
                        Thread.sleep(1);
                    }

                    // Check if to kill the process...
                    if (process.isAlive())
                    {
                        process.destroy();

                        LOG.warn("Killed process executing command, timeout exceeded - timeout: {}, cmd: {}",
                                processTimeout, commandsStr
                        );
                    }
                }

                return process;
            }
            catch (Exception e)
            {
                LOG.error("Failed to execute commands - command: {}", commandsStr, e);
            }
        }

        return null;
    }

    /**
     * Attempts to execute the command and return a float.
     *
     * If the output is not valid, or an exception occurs, null is returned.
     *
     * @param commands The command to execute
     * @param processTimeout
     * @param mockingEnabled Indicates if commands should be mocked on a dev machine
     * @return The value of the command, or null if mocked/invalid
     */
    public static Float execFloat(String[] commands, long processTimeout, boolean mockingEnabled)
    {
        String commandsStr = StringUtils.join(commands);

        if (mockingEnabled && isDevEnvironment())
        {
            LOG.debug("Mock executed command - commands: {}", commandsStr);
        }
        else
        {
            final long BUFFER_LIMIT = 512;

            Process process = null;

            try
            {
                // Run the process
                process = Runtime.getRuntime().exec(commands);

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
                while ((process.isAlive() || line != null) && System.currentTimeMillis() - start < processTimeout && buffer.length() < BUFFER_LIMIT);

                // Attempt to parse output
                if (buffer.length() > 0)
                {
                    try
                    {
                        return Float.parseFloat(buffer.toString());
                    }
                    catch (NumberFormatException e)
                    {
                        LOG.warn("Failed to parse command output - commands: {}", commands, e);
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
        }

        return null;
    }

    public static Dimension getScreenSize()
    {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();

        DisplayMode displayMode;
        for (GraphicsDevice graphicsDevice : graphicsDevices)
        {
            displayMode = graphicsDevice.getDisplayMode();
            return new Dimension(displayMode.getWidth(), displayMode.getHeight());
        }

        // Fallback...
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

}
