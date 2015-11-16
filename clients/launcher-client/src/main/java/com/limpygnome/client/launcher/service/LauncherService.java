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

/**
 * A service used to control the dashboard.
 */
public class LauncherService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(LauncherService.class);

    public static final String SERVICE_NAME = "launcher";

    private LauncherThread launcherThread;
    private Browser browser;
    private DashboardProvider dashboardProvider;

    @Override
    public void start(Controller controller)
    {
        // Setup dashboard provider
        dashboardProvider = DashboardProvider.load(controller);

        // Setup browser
        browser = new ChromiumBrowser();
        browser.setup(controller);

        // Start launcher thread to monitor and refresh dashboard
        launcherThread = new LauncherThread(controller, this);
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
        else if (restRequest.isPathMatch(new String[]{ "launcher-client", "url" }))
        {
            return handleRequestGetUrl(restRequest, restResponse);
        }

        return false;
    }

    private boolean handleRequestRefresh(RestRequest restRequest, RestResponse restResponse)
    {
        LOG.info("Refreshing browser from REST request...");

        browser.refresh();
        return true;
    }

    private boolean handleRequestGetUrl(RestRequest restRequest, RestResponse restResponse)
    {
        JSONObject response = new JSONObject();
        response.put("url", dashboardProvider.fetchPublicUrl());
        restResponse.writeJsonResponseIgnoreExceptions(restResponse, response);

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
