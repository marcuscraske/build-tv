package com.limpygnome.client.launcher.dashboard;

import org.json.simple.JSONObject;

/**
 * THe default implementation of a dashboard provider, which supports opening a URL.
 *
 * JSON parameter configuration:
 * - url: mandatory URL to be opened
 * - public.url: optional URL, which can be externally sent and shared. If not specified, url is inherited.
 */
public class DefaultDashbordProvider extends DashboardProvider
{
    private String url;
    private String publicUrl;

    @Override
    public void loadParams(JSONObject root)
    {
        // Setup URL
        url = (String) root.get("url");
        publicUrl = (String) root.get("public.url");

        // Setup time
        this.lifespan = (long) root.get("lifespan");

        if (url == null || url.length() == 0)
        {
            throw new RuntimeException("Dashboard URL not setup");
        }
        else if (publicUrl == null || publicUrl.length() == 0)
        {
            publicUrl = url;
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
