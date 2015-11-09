package com.limpygnome.daemon.util;

import java.io.File;
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
