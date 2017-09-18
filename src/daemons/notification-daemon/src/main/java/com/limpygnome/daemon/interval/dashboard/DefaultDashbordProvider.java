package com.limpygnome.daemon.interval.dashboard;

import org.json.simple.JSONObject;

/**
 * THe default implementation of a dashboards provider, which supports opening a URL.
 *
 * JSON parameter configuration:
 * - url: mandatory URL to be opened
 * - public.url: optional URL, which can be externally sent and shared. If not specified, url is inherited.
 */
public class DefaultDashbordProvider extends DashboardProvider
{
    private String url;
    private String publicUrl;

    /**
     * Creates a new instance.
     */
    public DefaultDashbordProvider() { }

    /**
     * Creates a new instance.
     *
     * @param url The URL
     * @param publicUrl The public URL; can be null to inherit URL
     * @param lifespan The lifespan of the dashboards, before it's transitioned
     * @param refresh The refresh interval of the dashboards
     */
    public DefaultDashbordProvider(String url, String publicUrl, long lifespan, long refresh)
    {
        this.url = url;
        this.publicUrl = publicUrl != null ? publicUrl : url;
        this.lifespan = lifespan;
        this.refresh = refresh;
    }

    @Override
    public void loadParams(JSONObject root)
    {
        super.loadParams(root);

        // Setup URL
        url = (String) root.get("url");
        publicUrl = (String) root.get("public.url");

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

    @Override
    public String toString()
    {
        return "[url: " + url + ", public url: " + publicUrl + "]";
    }

}
