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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

/**
 * A service used to control the dashboard.
 */
public class LauncherService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(LauncherService.class);

    private static final String DASHBOARD_CONFIG_FILE = "dashboard.json";

    public static final String SERVICE_NAME = "launcher";

    private LauncherThread launcherThread;
    private Browser browser;
    private DashboardProvider dashboardProvider;
    private JSONObject dashboardSettings;

    @Override
    public void start(Controller controller)
    {
        // Load dashboard config
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

        // Setup dashboard provider
        dashboardProvider = DashboardProvider.load(controller, dashboardSettings);

        // Setup browser
        browser = new ChromiumBrowser();
        browser.setup(controller);

        // Start launcher thread to monitor and refresh dashboard
        launcherThread = new LauncherThread(controller, this, dashboardSettings);
        launcherThread.start();
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

        dashboardProvider = null;
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
        return dashboardProvider;
    }

}
