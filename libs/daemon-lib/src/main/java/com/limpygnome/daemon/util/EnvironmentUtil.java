package com.limpygnome.daemon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by limpygnome on 20/07/15.
 */
public class EnvironmentUtil
{
    private static final Logger LOG = LogManager.getLogger(EnvironmentUtil.class);

    private static final boolean DEV_ENVIRONMENT;

    static
    {
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

}
