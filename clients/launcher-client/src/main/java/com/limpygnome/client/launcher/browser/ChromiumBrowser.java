package com.limpygnome.client.launcher.browser;

import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

/**
 * Chomium implementation of a browser.
 */
public class ChromiumBrowser implements Browser
{
    private static final Logger LOG = LogManager.getLogger(ChromiumBrowser.class);

    private Process currentWindow;
    private String currentUrl;

    @Override
    public void setup()
    {
        // Kill any process with a similar name
        EnvironmentUtil.exec(new String[]{ "pkill", "-9", "chromium-browser" }, 2000, false);
    }

    @Override
    public void openUrl(String url)
    {
        // Kill current window, if any
        kill();

        // Update current URL
        currentUrl = url;

        // Fetch screen size
        Dimension dimension = EnvironmentUtil.getScreenSize();

        // Start process and store as current window
        LOG.info("Opening URL - url: {}", url);

        currentWindow = EnvironmentUtil.exec(
            new String[]{
                    "chromium-browser",
                    "--kiosk",
                    "--disable-translate",
                    "--disable-session-crashed-bubble",
                    "--window-position=0,0",
                    "--window-size=" + dimension.getWidth() + "," + dimension.getHeight(),
                    url
            },
            0, false
        );
    }

    @Override
    public void refresh()
    {
        if (currentUrl != null)
        {
            LOG.debug("Refreshing URL - url: {}", currentUrl);

            // Launch new instance
            openUrl(currentUrl);
        }
    }

    @Override
    public void kill()
    {
        if (currentWindow != null && currentWindow.isAlive())
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
