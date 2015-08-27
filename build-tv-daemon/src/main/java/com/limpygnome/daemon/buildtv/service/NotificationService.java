package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.buildtv.model.Notification;

import java.awt.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by limpygnome on 27/08/15.
 */
public class NotificationService implements Service
{
    public static final String SERVICE_NAME = "notifications";

    private Notification currentNotification;

    @Override
    public void start(Controller controller)
    {
        // Set default startup notification to be hostname
        Notification notification = new Notification(
            getHostname(), "ip: " + getIpAddress(), 10000, Color.DARK_GRAY
        );

        updateCurrentNotification(notification);
    }

    @Override
    public void stop(Controller controller)
    {
        updateCurrentNotification(null);
    }

    public synchronized void updateCurrentNotification(Notification notification)
    {
        // Check notification is different
        if (notification == null || currentNotification == null || notification.isDifferentBesidesTimestamp(this.currentNotification))
        {
            this.currentNotification = notification;
        }
    }

    public synchronized Notification getCurrentNotification()
    {
        return currentNotification;
    }

    private String getHostname()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e)
        {
            return "unknown";
        }
    }

    public String getIpAddress()
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

            return resultIpAddress != null ? resultIpAddress : "unknown";
        }
        catch (Exception e)
        {
            return "unknown";
        }
    }
}
