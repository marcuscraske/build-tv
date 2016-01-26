package com.limpygnome.client.launcher.thread;

import com.limpygnome.client.launcher.browser.Browser;
import com.limpygnome.client.launcher.service.BrowserService;
import com.limpygnome.client.launcher.service.DashboardService;
import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ControllerState;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.common.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Used to monitor the dashboards and periodically restart it.
 */
public class DashboardHealthThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(DashboardHealthThread.class);

    /*
        The dashboards will only be refreshed if it was last refreshed greater than this period.

        Unit is milliseconds.
     */
    private long MINIMUM_REFRESH_PERIOD_MS = 60000;

    /*
        The rate at which this thread should sleep/run.
     */
    private long THREAD_SLEEP = 1000;

    private Controller controller;
    private BrowserService dashboardService;

    private long lastRefreshed;
    private long refreshHour;
    private long refreshMinute;

    public DashboardHealthThread(Controller controller, BrowserService dashboardService)
    {
        this.controller = controller;
        this.dashboardService = dashboardService;
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
        Browser browser = dashboardService.getBrowser();

        // Start browser...
        dashboardService.reloadBrowser();

        while (!isExit())
        {
            try
            {
                // Check if browser should be refreshed
                if (shouldRefreshBrowser(browser))
                {
                    dashboardService.reloadBrowser();
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

        if (dateTime.hourOfDay().get() == refreshHour && dateTime.minuteOfDay().get() == refreshMinute && (System.currentTimeMillis() - lastRefreshed) > MINIMUM_REFRESH_PERIOD_MS)
        {
            LOG.info("Time of day to refresh browser");
            return true;
        }

        return false;
    }

}
