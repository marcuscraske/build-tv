package com.limpygnome.daemon.buildtv.rest;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.buildtv.model.Notification;
import com.limpygnome.daemon.buildtv.service.NotificationService;
import com.limpygnome.daemon.util.StreamUtil;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.IOException;

/**
 * Created by limpygnome on 27/08/15.
 */
public class NotificationsRestHandler implements RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(NotificationsRestHandler.class);

    private NotificationService notificationService;

    @Override
    public void start(Controller controller)
    {
        notificationService = (NotificationService) controller.getServiceByName(NotificationService.SERVICE_NAME);
    }

    @Override
    public void stop(Controller controller)
    {
        notificationService = null;
    }

    @Override
    public boolean handleRequestInChain(HttpExchange httpExchange, JSONObject jsonRoot, String path)
    {
        // Check request destined for us
        if (!path.equals("/notifications"))
        {
            return false;
        }

        // Pull latest notification from service
        Notification notification = notificationService.getCurrentNotification();
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
            StreamUtil.writeJsonResponse(httpExchange, response);
        }
        catch (IOException e)
        {
            LOG.error("Failed to write response", e);
        }

        return true;
    }
}
