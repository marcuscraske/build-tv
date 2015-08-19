package com.limpygnome.daemon.screen.rest;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.screen.service.ScreenService;
import com.sun.net.httpserver.HttpExchange;
import org.json.simple.JSONObject;

/**
 * A REST handler used as an API to control the attached screen.
 */
public class ScreenRestHandler implements RestServiceHandler
{
    private ScreenService screenService;

    @Override
    public void start(Controller controller)
    {
        // Fetch screen service
        this.screenService = (ScreenService) controller.getServiceByName("screen");
    }

    @Override
    public void stop(Controller controller)
    {
        this.screenService = null;
    }

    @Override
    public boolean handleRequestInChain(HttpExchange httpExchange, JSONObject jsonRoot)
    {
        String action = (String) jsonRoot.get("action");

        if (action != null && action.length() > 0)
        {
            switch (action)
            {
                case "on":
                    screenService.screenOn();
                    break;
                case "off":
                    screenService.screenOff();
                    break;
            }
        }

        return true;
    }

}
