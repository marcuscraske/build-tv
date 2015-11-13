package com.limpygnome.client.launcher.service;

import com.limpygnome.client.launcher.browser.Browser;
import com.limpygnome.client.launcher.browser.ChromiumBrowser;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.rest.RestRequest;
import com.limpygnome.daemon.common.rest.RestResponse;

/**
 * Created by limpygnome on 13/11/15.
 */
public class LauncherService implements Service, RestServiceHandler
{
    public static final String SERVICE_NAME = "launcher";

    private Browser browser;

    /* URL with authentication etc, which must be kept privately. */
    private String dashboardUrl;

    /* A URL without any authentication etc, which can be shared publicly. */
    private String dashboardPublicUrl;

    @Override
    public void start(Controller controller)
    {
        // Retrieve dashboard URL
        // Consider redoing this, so that we can load a JIRA dashboard, or just a URL...
        controller.getSettings().getString("dashboard/url");

        // Setup browser
        browser = new ChromiumBrowser();
        browser.setup();
    }

    @Override
    public void stop(Controller controller)
    {
        dashboardUrl = null;
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        /*
            need to allow:
            - remote service to retrieve public dashboard URL
         */
        return false;
    }

    public void launchDashboard()
    {
        browser.openUrl(dashboardUrl);
    }

}
