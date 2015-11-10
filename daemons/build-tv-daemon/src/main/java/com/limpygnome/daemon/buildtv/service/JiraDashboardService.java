package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.rest.RestRequest;
import com.limpygnome.daemon.common.rest.RestResponse;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * A service for retrieving the current Jira dashboard ID used for the wallboard.
 */
public class JiraDashboardService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(JiraDashboardService.class);

    public static final String SERVICE_NAME = "jira-dashboard";

    /* The key of the dashboard ID property in the wallboard properties file. */
    private static final String WALLBOARD_PROPERTIES_DASHBOARDID_KEY = "JIRA_WALLBOARD_ID";

    /* The mock ID used when the properties file does not exist and we're running on a dev machine. */
    private static final long MOCKED_DASHBOARD_ID = 10000;

    private long dashboardId;

    @Override
    public void start(Controller controller)
    {
        // Fetch path of properties file
        String wallboardPropertiesPath = controller.getSettings().getString("jira.wallboard.path");

        // Read the current dashboard
        try
        {
            File wallboardPropertiesFile = new File(wallboardPropertiesPath);

            if (wallboardPropertiesFile.exists())
            {
                Properties properties = new Properties();
                properties.load(new FileInputStream(wallboardPropertiesFile));
                dashboardId = Long.parseLong(properties.getProperty(WALLBOARD_PROPERTIES_DASHBOARDID_KEY));

                LOG.info("Retrieved Jira dashboard ID - dashboard ID: {}", dashboardId);
            }
            else
            {
                LOG.warn("Wallboard properties file missing, cannot retrieve Jira wallboard - path: {}", wallboardPropertiesPath);

                if (EnvironmentUtil.isDevEnvironment())
                {
                    this.dashboardId = MOCKED_DASHBOARD_ID;
                    LOG.info("Mocked Jira dashboard ID, development machine detected - mocked dashboard ID: {}", dashboardId);
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to read Jira dashboard ID", e);
        }
    }

    @Override
    public void stop(Controller controller)
    {
        dashboardId = 0;
    }

    public long getDashboardId()
    {
        return dashboardId;
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        if (!restRequest.isPathMatch(new String[]{ "build-tv-daemon", "dashboard", "get" }))
        {
            return false;
        }

        JSONObject response = new JSONObject();
        response.put("id", dashboardId);

        restResponse.writeJsonResponseIgnoreExceptions(restResponse, response);

        return true;
    }

}
