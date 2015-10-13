package com.limpygnome.daemon.remote.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.remote.model.DaemonType;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by limpygnome on 13/10/15.
 */
public class RestForwarderService implements Service
{
    private Map<DaemonType, String> daemonUrls;

    @Override
    public void start(Controller controller)
    {
        // Build URLs for local daemons using settings
        daemonUrls = new HashMap<>();

        for (DaemonType daemonType : DaemonType.values())
        {
            daemonUrls.put(daemonType, controller.getSettings().getString(daemonType.SETTING_KEY_PORT));
        }
    }

    @Override
    public void stop(Controller controller)
    {
        daemonUrls.clear();
        daemonUrls = null;
    }

    public JSONObject forward(DaemonType daemonType, JSONObject request, String path)
    {
    }



}
