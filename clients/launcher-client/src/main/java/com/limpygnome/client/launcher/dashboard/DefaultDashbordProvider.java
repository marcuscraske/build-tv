package com.limpygnome.client.launcher.dashboard;

import org.json.simple.JSONObject;

/**
 * Created by limpygnome on 16/11/15.
 */
public class DefaultDashbordProvider extends DashboardProvider
{
    private String url;
    private String publicUrl;

    @Override
    public void loadParams(JSONObject root)
    {
        url = (String) root.get("url");
        publicUrl = (String) root.get("public.url");

        if (url == null || url.length() == 0)
        {
            throw new RuntimeException("Dashboard URL not setup");
        }
        else if (publicUrl == null || publicUrl.length() == 0)
        {
            throw new RuntimeException("Public dashboard URL not setup");
        }
    }

    @Override
    public String fetchUrl()
    {
        return url;
    }

    @Override
    public String fetchPublicUrl()
    {
        return publicUrl;
    }

}
