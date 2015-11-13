package com.limpygnome.client.launcher.dashboard;

import org.json.simple.JSONObject;

import java.util.Map;

/**
 * Created by limpygnome on 13/11/15.
 */
public interface DashboardProvider
{

    /**
     * Loads parameters for the provider from the provided JSON object.
     *
     * @param root Root element with parameters
     */
    void loadParams(JSONObject root);

    /**
     * Builds the URL for the dashboard, which is used to display the dashboard on the attached monitor.
     *
     * Can contain sensitive information.
     *
     * @return The dashboard URL
     */
    String buildUrl();

    /**
     * 
     * @return
     */
    String buildPublicUrl();

}
