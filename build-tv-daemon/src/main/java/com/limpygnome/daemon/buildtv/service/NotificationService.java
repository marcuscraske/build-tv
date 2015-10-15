package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.api.rest.RestRequest;
import com.limpygnome.daemon.api.rest.RestResponse;
import com.limpygnome.daemon.api.rest.RestServiceHandler;
import com.limpygnome.daemon.buildtv.model.Notification;

import com.limpygnome.daemon.util.StreamUtil;
import com.sun.net.httpserver.HttpExchange;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * Created by limpygnome on 27/08/15.
 */
public class NotificationService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(NotificationService.class);

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

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        // Check request destined for us
        if (!restRequest.isPathMatch(new String[]{ "build-tv-daemon", "notifications" }))
        {
            return false;
        }

        // Pull latest notification from service
        Notification notification = getCurrentNotification();
        Color background = notification.getBackground();

        // Build into response message
        JSONObject response = new JSONObject();

        response.put("timestamp", notification.getTimeStamp());
        response.put("header", notification.getHeader());
        response.put("text" , notification.getText());
        response.put("lifespan", notification.getLifespan());

        JSONObject resonseBackground = new JSONObject();
        resonseBackground.put("r", background.getRed());
        resonseBackground.put("g", background.getGreen());
        resonseBackground.put("b", background.getBlue());

        response.put("background", resonseBackground);

        // Write response
        try
        {
            restResponse.writeJsonResponse(restResponse, response);
        }
        catch (IOException e)
        {
            LOG.error("Failed to write response", e);
        }

        return true;
    }

}
