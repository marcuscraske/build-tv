package com.limpygnome.client.launcher.service;

import com.limpygnome.client.launcher.browser.Browser;
import com.limpygnome.client.launcher.browser.ChromiumBrowser;
import com.limpygnome.client.launcher.browser.MockBrowser;
import com.limpygnome.client.launcher.thread.DashboardHealthThread;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;

/**
 * A service for controlling the browser for rendering dashboards.
 */
public class BrowserService implements Service
{
    /* Thread used to monitor health of dashboards. */
    private DashboardHealthThread dashboardHealthThread;

    /* The browser displaying the dashboards. */
    private Browser browser;

    /* Web server service, used to retrieve URL for rendering dashboards. */
    private WebServerService webServerService;

    @Override
    public void start(Controller controller)
    {
        // Fetch webserver service
        webServerService = (WebServerService) controller.getServiceByName(WebServerService.SERVICE_NAME);

        // Setup browser
        String browserSetting = controller.getSettings().getString("browser");

        switch (browserSetting)
        {
            case ChromiumBrowser.BROWSER_NAME:
                browser = new ChromiumBrowser();
                break;
            case MockBrowser.BROWSER_NAME:
                browser = new MockBrowser();
                break;
            default:
                throw new IllegalArgumentException("Browser '" + browserSetting + "' not available");
        }

        browser.setup(controller);

        // Start launcher thread to monitor and refresh dashboards
        dashboardHealthThread = new DashboardHealthThread(controller, this);
        dashboardHealthThread.start();
    }

    @Override
    public void stop(Controller controller)
    {
        // Destroy thread
        if (dashboardHealthThread != null)
        {
            dashboardHealthThread.kill();
            dashboardHealthThread = null;
        }

        // Destroy browser
        if (browser != null)
        {
            browser.kill();
            browser = null;
        }

        webServerService = null;
    }

    public void reloadBrowser()
    {
        browser.openUrl(webServerService.getWebserverUrl());
    }

    public Browser getBrowser()
    {
        return browser;
    }

}
