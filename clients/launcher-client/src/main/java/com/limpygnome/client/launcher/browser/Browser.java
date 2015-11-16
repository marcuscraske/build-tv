package com.limpygnome.client.launcher.browser;

import com.limpygnome.daemon.api.Controller;

/**
 * Used to create and control a browser for displaying URLs.
 *
 * Ideally this should be replaced by Selenium, but the Raspberry Pi's ARM architecture requires manual
 * compilation, thus this is easier.
 */
public interface Browser
{

    /**
     * Initially sets up the browser.
     */
    void setup(Controller controller);

    /**
     * Opens a URL in the browser.
     *
     * @param url The URL
     */
    void openUrl(String url);

    /**
     * Refreshes the current URL.
     */
    void refresh();

    /**
     * Checks the browser is still alive.
     *
     * @return True = alive, false = process no longer active
     */
    boolean isAlive();

    /**
     * KIlls the current window.
     */
    void kill();

}
