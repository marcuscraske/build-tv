package com.limpygnome.daemon.interval.service;

import com.limpygnome.daemon.interval.dashboard.DashboardProvider;
import com.limpygnome.daemon.interval.dashboard.DefaultDashbordProvider;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.rest.RestRequest;
import com.limpygnome.daemon.common.rest.RestResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/**
 * A service used to control the dashboards.
 */
public class DashboardService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(DashboardService.class);

    private static final String DASHBOARD_CONFIG_FILE = "dashboards.json";

    public static final String SERVICE_NAME = "dashboards";

    private JSONObject dashboardSettings;

    /* Available dashboards providers. */
    private DashboardProvider[] dashboardProviders;

    /* Used for when dashboards URLs are manually set via REST service. */
    private DashboardProvider[] overrideDashboardProviders;

    @Override
    public synchronized void start(Controller controller)
    {
        // Load dashboards config
        loadDashboardConfig(controller);

        // Parse dashboards providers
        parseProviders(controller);
    }

    private synchronized void loadDashboardConfig(Controller controller)
    {
        try
        {
            File dashboardConfigFile = controller.getFilePathConfig(DASHBOARD_CONFIG_FILE);
            JSONParser jsonParser = new JSONParser();
            dashboardSettings = (JSONObject) jsonParser.parse(new FileReader(dashboardConfigFile));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to read dashboard configuration - file: " + DASHBOARD_CONFIG_FILE, e);
        }
    }

    private synchronized void parseProviders(Controller controller)
    {
        JSONArray dashboardPages = (JSONArray) dashboardSettings.get("pages");

        List<DashboardProvider> dashboardProviders = new LinkedList<>();
        DashboardProvider dashboardProvider;
        JSONObject page;

        for (Object rawElement : dashboardPages)
        {
            // Fetch config for page / dashboards provider
            page = (JSONObject) rawElement;

            // Parse provider
            dashboardProvider = DashboardProvider.parse(controller, page);
            dashboardProviders.add(dashboardProvider);
        }

        // Setup final array
        this.dashboardProviders = dashboardProviders.toArray(new DashboardProvider[dashboardProviders.size()]);
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        overrideDashboardProviders = null;
        dashboardProviders = null;
    }

    @Override
    public synchronized boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        if (restRequest.isPathMatch(new String[]{ "interval-daemon", "dashboards", "urls", "get" }))
        {
            return handleRequestGetUrls(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "interval-daemon", "dashboards", "urls", "set" }))
        {
            return handleRequestOpenUrls(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "interval-daemon", "dashboards", "urls", "reset" }))
        {
            return handleRequestResetUrls(restRequest, restResponse);
        }

        return false;
    }

    private synchronized boolean handleRequestOpenUrls(RestRequest restRequest, RestResponse restResponse)
    {
        JSONArray requestDashboards = (JSONArray) restRequest.getJsonElement(new String[]{ "dashboards" });

        DashboardProvider[] dashboardProviders = new DashboardProvider[requestDashboards.size()];

        Object rawRequestDashboard;
        JSONObject requestDashboard;

        String url;
        String publicUrl;
        long lifespan;
        long refresh;

        for (int i = 0; i < requestDashboards.size(); i++)
        {
            rawRequestDashboard = requestDashboards.get(i);
            requestDashboard = (JSONObject) rawRequestDashboard;

            // Fetch URLs
            url = (String) requestDashboard.get("url");
            publicUrl = (String) requestDashboard.get("public_url");
            lifespan = (long) requestDashboard.get("lifespan");
            refresh = (long) requestDashboard.get("refresh");

            // Create provider...
            dashboardProviders[i] = new DefaultDashbordProvider(url, publicUrl, lifespan, refresh);
        }

        // Switch override dashboards...
        overrideDashboardProviders = dashboardProviders;

        LOG.info("Dashboards override from REST request... - providers: {}", StringUtils.join(overrideDashboardProviders, ","));

        return true;
    }

    private synchronized boolean handleRequestGetUrls(RestRequest restRequest, RestResponse restResponse)
    {
        // Fetch current dashboards
        DashboardProvider[] dashboardProviders = getDashboardProviders();

        // Build response
        JSONObject response = new JSONObject();
        JSONArray responseDashboards = new JSONArray();
        JSONObject responseDashboard;

        for (DashboardProvider dashboardProvider : dashboardProviders)
        {
            responseDashboard = new JSONObject();

            responseDashboard.put("url", dashboardProvider.fetchUrl());
            responseDashboard.put("public_url", dashboardProvider.fetchPublicUrl());
            responseDashboard.put("lifespan", dashboardProvider.getLifespan());
            responseDashboard.put("refresh", dashboardProvider.getRefresh());

            responseDashboards.add(responseDashboard);
        }
        response.put("dashboards", responseDashboards);

        restResponse.writeJsonResponseIgnoreExceptions(response);

        return true;
    }

    private synchronized boolean handleRequestResetUrls(RestRequest restRequest, RestResponse restResponse)
    {
        LOG.info("Reset override dashboards, reloading dashboards from REST request...");

        // Reset override dashboards
        overrideDashboardProviders = null;

        return true;
    }

    /**
     * Retrieves the current dashboards providers.
     *
     * This is not a simple accessor, so invocations should be held/cached where/when appropriate.
     *
     * @return The current dashboards provider
     */
    public synchronized DashboardProvider[] getDashboardProviders()
    {
        if (overrideDashboardProviders != null)
        {
            return overrideDashboardProviders;
        }
        else
        {
            return dashboardProviders;
        }
    }

}
