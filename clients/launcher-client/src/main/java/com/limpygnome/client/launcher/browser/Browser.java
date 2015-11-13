package com.limpygnome.client.launcher.browser;

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
    void setup();

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
     * KIlls the current window.
     */
    void kill();

}
