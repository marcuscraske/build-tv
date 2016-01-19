package com.limpygnome.client.launcher.service;

import com.limpygnome.client.launcher.browser.Browser;
import com.limpygnome.client.launcher.browser.ChromiumBrowser;
import com.limpygnome.client.launcher.dashboard.DashboardProvider;
import com.limpygnome.client.launcher.dashboard.DefaultDashbordProvider;
import com.limpygnome.client.launcher.thread.LauncherThread;
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
 * A service used to control the dashboard.
 */
public class LauncherService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(LauncherService.class);

    private static final String DASHBOARD_CONFIG_FILE = "dashboards.json";

    public static final String SERVICE_NAME = "launcher";

    private JSONObject dashboardSettings;
    private LauncherThread launcherThread;
    private Browser browser;

    /* Available dashboard providers. */
    private DashboardProvider[] dashboardProviders;

    /* Used for when dashboard URLs are manually set via REST service. */
    private DashboardProvider[] overrideDashboardProviders;

    /* Web server service, used to retrieve URL for rendering dashboards. */
    private WebServerService webServerService;

    @Override
    public synchronized void start(Controller controller)
    {
        // Fetch webserver service
        webServerService = (WebServerService) controller.getServiceByName(WebServerService.SERVICE_NAME);

        // Load dashboard config
        loadDashboardConfig(controller);

        // Parse dashboard providers
        parseProviders(controller);

        // Setup browser
        browser = new ChromiumBrowser();
        browser.setup(controller);

        // Start launcher thread to monitor and refresh dashboard
        launcherThread = new LauncherThread(controller, this);
        launcherThread.start();
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
            // Fetch config for page / dashboard provider
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
        // Destroy thread
        if (launcherThread != null)
        {
            launcherThread.kill();
            launcherThread = null;
        }

        // Destroy browser
        if (browser != null)
        {
            browser.kill();
            browser = null;
        }

        overrideDashboardProviders = null;
        dashboardProviders = null;
        webServerService = null;
    }

    @Override
    public synchronized boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        if (restRequest.isPathMatch(new String[]{ "launcher-client", "refresh" }))
        {
            return handleRequestRefresh(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "launcher-client", "urls", "get" }))
        {
            return handleRequestGetUrls(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "launcher-client", "kill" }))
        {
            return handleRequestKill(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "launcher-client", "urls", "set" }))
        {
            return handleRequestOpenUrls(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "launcher-client", "urls", "reset" }))
        {
            return handleRequestResetUrls(restRequest, restResponse);
        }

        return false;
    }

    private synchronized boolean handleRequestRefresh(RestRequest restRequest, RestResponse restResponse)
    {
        LOG.info("Refreshing browser from REST request...");

        browser.refresh();
        return true;
    }

    private synchronized boolean handleRequestKill(RestRequest restRequest, RestResponse restResponse)
    {
        LOG.info("Killing browser from REST request...");

        browser.kill();
        return true;
    }

    private synchronized boolean handleRequestOpenUrls(RestRequest restRequest, RestResponse restResponse)
    {
        JSONArray requestDashboards = (JSONArray) restRequest.getJsonElement(new String[]{ "dashboards" });

        DashboardProvider[] dashboardProviders = new DashboardProvider[requestDashboards.size()];

        Object rawRequestDashboard;
        JSONObject requestDashboard;
        String url;
        String publicUrl;

        for (int i = 0; i < requestDashboards.size(); i++)
        {
            rawRequestDashboard = requestDashboards.get(i);
            requestDashboard = (JSONObject) rawRequestDashboard;

            // Fetch URLs
            url = (String) requestDashboard.get("url");
            publicUrl = (String) requestDashboard.get("public_url");

            // Create provider...
            dashboardProviders[i] = new DefaultDashbordProvider(url, publicUrl);
        }

        // Switch override dashboards...
        overrideDashboardProviders = dashboardProviders;

        LOG.info("Dashboards override from REST request... - providers: {}", StringUtils.join(overrideDashboardProviders, ","));

        return true;
    }

    private synchronized boolean handleRequestGetUrls(RestRequest restRequest, RestResponse restResponse)
    {
        // Fetch current dashboard
        DashboardProvider[] dashboardProviders = getDashboardProviders();

        // Build response
        JSONObject response = new JSONObject();
        JSONArray responseDashboards = new JSONArray();
        JSONObject responseDashboard;

        for (DashboardProvider dashboardProvider : dashboardProviders)
        {
            responseDashboard = new JSONObject();
            responseDashboard.put("url", dashboardProvider.fetchPublicUrl());
            responseDashboards.add(responseDashboard);
        }
        response.put("dashboards", responseDashboards);

        restResponse.writeJsonResponseIgnoreExceptions(restResponse, response);

        return true;
    }

    private synchronized boolean handleRequestResetUrls(RestRequest restRequest, RestResponse restResponse)
    {
        LOG.info("Reset override dashboards, reloading dashboards from REST request...");

        // Reset override dashboards
        overrideDashboardProviders = null;

        // Re-open dashboard renderer...
        reloadBrowser();

        return true;
    }

    public void reloadBrowser()
    {
        browser.openUrl(webServerService.getWebserverUrl());
    }

    public Browser getBrowser()
    {
        return browser;
    }

    /**
     * Retrieves the current dashboard providers.
     *
     * This is not a simple accessor, so invocations should be held/cached where/when appropriate.
     *
     * @return The current dashboard provider
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
