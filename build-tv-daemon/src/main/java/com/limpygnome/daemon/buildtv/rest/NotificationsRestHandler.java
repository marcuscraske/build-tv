package com.limpygnome.daemon.buildtv.rest;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.rest.RestServiceHandler;
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


}
