package com.limpygnome.client.launcher.browser;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

/**
 * Chromium implementation of a browser.
 */
public class ChromiumBrowser implements Browser
{
    private static final Logger LOG = LogManager.getLogger(ChromiumBrowser.class);

    public static final String BROWSER_NAME = "chromium";

    private Process currentWindow;
    private String currentUrl;

    @Override
    public synchronized void setup(Controller controller)
    {
        // Kill any process with a similar name
        LOG.debug("Killing any processes with a similar process name...");
        EnvironmentUtil.exec(new String[]{ "pkill", "-9", "chromium-browser" }, 2000, false);
    }

    @Override
    public synchronized void openUrl(String url)
    {
        // Determine we have a URL
        if (url != null && url.length() > 0)
        {
            // Kill current window, if any
            kill();

            // Update current URL
            currentUrl = url;

            // Fetch screen size
            Dimension dimension = EnvironmentUtil.getScreenSize();

            long dimensionWidth = (long) dimension.getWidth();
            long dimensionHeight = (long) dimension.getHeight();

            // Start process and store as current window
            LOG.info("Opening URL - url: {}", url);

            currentWindow = EnvironmentUtil.exec(
                    new String[]{
                            "chromium-browser",
                            "--no-default-browser-check",
                            "--disable-save-password-bubble",
                            "--kiosk",
                            "--incognito",
                            "--disable-translate",
                            "--disable-session-crashed-bubble",
                            "--window-position=0,0",
                            "--window-size=" + dimensionWidth + "," + dimensionHeight,
                            url
                    },
                    0, false
            );
        }
        else
        {
            LOG.warn("An invalid URL has been passed, not opening new browser window...");
        }
    }

    @Override
    public String getCurrentUrl() {
        return currentUrl;
    }

    @Override
    public synchronized void refresh()
    {
        if (currentUrl != null)
        {
            LOG.debug("Refreshing URL - url: {}", currentUrl);

            // Launch new instance
            openUrl(currentUrl);
        }
    }

    @Override
    public boolean isAlive()
    {
        return currentWindow != null && EnvironmentUtil.isAlive(currentWindow);
    }

    @Override
    public synchronized void kill()
    {
        if (currentWindow != null && EnvironmentUtil.isAlive(currentWindow))
        {
            try
            {
                currentWindow.destroy();
                currentWindow = null;
                currentUrl = null;

                LOG.debug("Destroyed current window");
            }
            catch (Exception e)
            {
                LOG.error("Failed to kill current process", e);
            }
        }
    }

}
