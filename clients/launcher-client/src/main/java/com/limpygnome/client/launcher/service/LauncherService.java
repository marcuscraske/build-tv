package com.limpygnome.client.launcher.service;

import com.limpygnome.client.launcher.browser.Browser;
import com.limpygnome.client.launcher.browser.ChromiumBrowser;
import com.limpygnome.client.launcher.dashboard.DashboardProvider;
import com.limpygnome.client.launcher.thread.LauncherThread;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.rest.RestRequest;
import com.limpygnome.daemon.common.rest.RestResponse;
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

    private static final String DASHBOARD_CONFIG_FILE = "dashboard.json";

    public static final String SERVICE_NAME = "launcher";

    private JSONObject dashboardSettings;
    private LauncherThread launcherThread;
    private Browser browser;
    private DashboardProvider[] dashboardProviders;
    /* Used for when URLs are manually set via REST service. */
    private DashboardProvider overrideDashboard;
    private int currentDashboardProviderIndex;
    private long currentDashboardProviderChanged;

    @Override
    public void start(Controller controller)
    {
        // Load dashboard config
        loadDashboardConfig(controller);

        // Parse dashboard providers
        parseProviders(controller);

        // Setup browser
        browser = new ChromiumBrowser();
        browser.setup(controller);

        // Start launcher thread to monitor and refresh dashboard
        launcherThread = new LauncherThread(controller, this, dashboardSettings);
        launcherThread.start();
    }

    private void loadDashboardConfig(Controller controller)
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

    private void parseProviders(Controller controller)
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
        this.currentDashboardProviderIndex = 0;
        this.currentDashboardProviderChanged = System.currentTimeMillis();
    }

    @Override
    public void stop(Controller controller)
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

        dashboardProviders = null;
        currentDashboardProviderIndex = -1;
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        if (restRequest.isPathMatch(new String[]{ "launcher-client", "refresh" }))
        {
            return handleRequestRefresh(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "launcher-client", "url", "get" }))
        {
            return handleRequestGetUrl(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "launcher-client", "kill" }))
        {
            return handleRequestKill(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "launcher-client", "url", "set" }))
        {
            return handleRequestOpenUrl(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "launcher-client", "url", "reset" }))
        {
            return handleRequestResetToDashboard(restRequest, restResponse);
        }

        return false;
    }

    private boolean handleRequestRefresh(RestRequest restRequest, RestResponse restResponse)
    {
        LOG.info("Refreshing browser from REST request...");

        browser.refresh();
        return true;
    }

    private boolean handleRequestKill(RestRequest restRequest, RestResponse restResponse)
    {
        LOG.info("Killing browser from REST request...");

        browser.kill();
        return true;
    }

    private boolean handleRequestOpenUrl(RestRequest restRequest, RestResponse restResponse)
    {
        String url = (String) restRequest.getJsonElement(new String[]{ "url" });
        LOG.info("Opening URL in browser from REST request... - url: {}", url);

        browser.openUrl(url);
        return true;
    }

    private boolean handleRequestGetUrl(RestRequest restRequest, RestResponse restResponse)
    {
        // Fetch current dashboard
        DashboardProvider dashboardProvider = getDashboardProvider();

        // Build response
        JSONObject response = new JSONObject();
        response.put("url", dashboardProvider.fetchPublicUrl());
        restResponse.writeJsonResponseIgnoreExceptions(restResponse, response);

        return true;
    }

    private boolean handleRequestResetToDashboard(RestRequest restRequest, RestResponse restResponse)
    {
        LOG.info("Resetting browser to dashboard from REST request...");

        String dashboardUrl = dashboardProvider.fetchUrl();
        browser.openUrl(dashboardUrl);

        return true;
    }

    public Browser getBrowser()
    {
        return browser;
    }

    public DashboardProvider getDashboardProvider()
    {
        we need to add zero dashboard protection...when loading...
        DashboardProvider dashboardProvider;

        if (overrideDashboard != null)
        {
            dashboardProvider = overrideDashboard;
        }
        else
        {
            // Determine if current dashboard has expired

            dashboardProvider = dashboardProviders[currentDashboardProviderIndex];
        }

        return dashboardProvider;
    }

}
