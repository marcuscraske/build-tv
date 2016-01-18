package com.limpygnome.client.launcher.dashboard;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * The JIRA implementation of a dashboard provider, which can build public and private URLs for a Jira dashboard.
 *
 * This will use the Atlassian Wallboard plugin to display a dashboard.
 *
 * JSON parameter configuration:
 * - user: the Jira user able to access the dashboard
 * - pass: the pass for the Jira user
 * - url: the base URL of the Jira instance
 * - dashboard: the ID of the dashboard
 */
public class JiraDashboardProvider extends DashboardProvider
{
    private static final String JIRA_URL = "${url}/login.jsp?os_username=${user}&os_password=${pass}&os_destination=plugins/servlet/Wallboard/?dashboardId=${dashboard}";
    private static final String JIRA_PUBLIC_URL = "${url}/plugins/servlet/Wallboard/?dashboardId=${dashboard}";

    private String url;
    private String publicUrl;

    @Override
    public void loadParams(JSONObject root)
    {
        // Prepare to build URLs
        Map<String, String> values = new HashMap<>();
        values.put("user", (String) root.get("user"));
        values.put("pass", (String) root.get("pass"));
        values.put("url", (String) root.get("url"));
        values.put("dashboard", (String) root.get("dashboard"));

        // Setup time
        this.lifespan = (long) root.get("lifespan");

        // Build URLs
        StrSubstitutor substitutor = new StrSubstitutor(values);

        url = substitutor.replace(JIRA_URL);
        publicUrl = substitutor.replace(JIRA_PUBLIC_URL);
    }

    @Override
    public String fetchUrl()
    {
        return url;
    }

    @Override
    public String fetchPublicUrl()
    {
        return publicUrl;
    }
}
