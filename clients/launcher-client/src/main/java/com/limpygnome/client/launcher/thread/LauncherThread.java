package com.limpygnome.client.launcher.thread;

import com.limpygnome.client.launcher.browser.Browser;
import com.limpygnome.client.launcher.dashboard.DashboardProvider;
import com.limpygnome.client.launcher.service.LauncherService;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ControllerState;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.common.Settings;
import com.limpygnome.daemon.util.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

/**
 * Used to monitor the dashboard and periodically restart it.
 */
public class LauncherThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(LauncherThread.class);

    /*
        The dashboard will only be refreshed if it was last refreshed greater than this period.

        Unit is milliseconds.
     */
    private long MINIMUM_REFRESH_PERIOD_MS = 60000;

    /*
        The rate at which this thread should sleep/run.
     */
    private long THREAD_SLEEP = 1000;

    private Controller controller;
    private LauncherService launcherService;
    private long lastRefreshed;
    private long refreshHour;
    private long refreshMinute;

    public LauncherThread(Controller controller, LauncherService launcherService, JSONObject dashboardSettings)
    {
        this.controller = controller;
        this.launcherService = launcherService;
        this.lastRefreshed = System.currentTimeMillis();

        Settings settings = controller.getSettings();

        // Read refresh hour/minute
        refreshHour = settings.getOptionalLong("dashboard/refresh/hour", 0);
        refreshMinute = settings.getOptionalLong("dashboard/refresh/minute", 0);
    }

    @Override
    public void run()
    {
        // Wait for controller to start...
        LOG.debug("Waiting for controller to be running...");
        controller.waitForState(ControllerState.RUNNING);

        // Retrieve browser and provider
        Browser browser = launcherService.getBrowser();

        // Monitor URL of current dashboard, health of browser and periodically refresh
        DashboardProvider dashboardProvider;

        while (!isExit())
        {
            try
            {
                // Retrieve the current dashboard provider
                dashboardProvider = launcherService.getDashboardProvider();

                // Check if to change dashboard or if browser should be refreshed
                // -- If the dashboard URL changes, method invokes URL on browser, not a refresh
                if (!dashboardUrlChanged(dashboardProvider, browser) && shouldRefreshBrowser(browser))
                {
                    browser.refresh();
                    lastRefreshed = System.currentTimeMillis();
                }

                Thread.sleep(THREAD_SLEEP);
            }
            catch (InterruptedException e)
            {
                // We don't care...
            }
        }
    }

    private boolean shouldRefreshBrowser(Browser browser)
    {
        // Determine if to refresh based on browser being dead
        if (!browser.isAlive())
        {
            LOG.info("Browser is not alive");
            return true;
        }

        // Determine if to refresh based on time
        DateTime dateTime = DateTime.now();
        if (dateTime.hourOfDay().get() == 0 && dateTime.minuteOfDay().get() == 0 && (System.currentTimeMillis() - lastRefreshed) > MINIMUM_REFRESH_PERIOD_MS)
        {
            LOG.info("Time of day to refresh browser");
            return true;
        }

        return false;
    }

    private boolean dashboardUrlChanged(DashboardProvider dashboardProvider, Browser browser)
    {
        String browserUrl = browser.getCurrentUrl();
        String currentUrl = dashboardProvider.fetchUrl();

        // Check if dashboard URL is available and has changed
        if (currentUrl != null && !currentUrl.equals(browserUrl))
        {
            LOG.info("Dashboard URL has changed...");

            // Open new URL in browser
            browser.openUrl(currentUrl);

            return true;
        }

        return false;
    }

}
