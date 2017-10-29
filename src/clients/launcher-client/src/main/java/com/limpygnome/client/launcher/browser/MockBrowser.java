package com.limpygnome.client.launcher.browser;

import com.limpygnome.daemon.api.Controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mock implementation of a browser, useful for development.
 */
public class MockBrowser implements Browser
{
    private static final Logger LOG = LogManager.getLogger(ChromiumBrowser.class);

    public static final String BROWSER_NAME = "mock";

    @Override
    public void setup(Controller controller)
    {
        LOG.info("mock browser - setup invoked");
    }

    @Override
    public void openUrl(String url)
    {
        LOG.info("mock browser - open URL invoked - url: {}", url);
    }

    @Override
    public String getCurrentUrl()
    {
        return null;
    }

    @Override
    public void refresh()
    {
        LOG.info("mock browser - refresh invoked");
    }

    @Override
    public boolean isAlive()
    {
        return true;
    }

    @Override
    public void kill()
    {
        LOG.info("mock browser - kill invoked");
    }

}
