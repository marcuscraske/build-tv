package com.limpygnome.daemon.remote.service.thread;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ControllerState;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.remote.model.ComponentType;
import com.limpygnome.daemon.remote.service.HostInformationService;
import com.limpygnome.daemon.remote.service.StatsForwarderService;
import com.limpygnome.daemon.remote.service.InstanceIdentityService;
import com.limpygnome.daemon.remote.service.VersionService;
import com.limpygnome.daemon.remote.service.auth.AuthTokenProviderService;
import com.limpygnome.daemon.util.JsonUtil;
import com.limpygnome.daemon.util.RestClient;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;

/**
 * A thread responsible for initially sending information about the system to an endpoint. Periodic updates regarding
 * the system are sent to the same endpoint too.
 */
public class StatsForwarderThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(StatsForwarderThread.class);

    private static final String STATS_FORWARDER_USER_AGENT = "stats-forwarder";

    private Controller controller;
    private StatsForwarderService statsForwarderService;
    private InstanceIdentityService instanceIdentityService;
    private AuthTokenProviderService authProviderService;
    private VersionService versionService;
    private HostInformationService hostInformationService;

    private String ledDaemonPatternEndpointUrl;
    private String systemDaemonStatsEndpointUrl;
    private String systemDaemonScreenGetEndpointUrl;
    private String intervalServiceDashboardEndpointUrl;

    private JSONArray cacheDashboardUrls;

    public StatsForwarderThread(Controller controller, StatsForwarderService statsForwarderService)
    {
        this.controller = controller;
        this.statsForwarderService = statsForwarderService;
        this.instanceIdentityService = (InstanceIdentityService) controller.getServiceByName(InstanceIdentityService.SERVICE_NAME);
        this.authProviderService = (AuthTokenProviderService) controller.getServiceByName(AuthTokenProviderService.SERVICE_NAME);
        this.versionService = (VersionService) controller.getServiceByName(VersionService.SERVICE_NAME);
        this.hostInformationService = (HostInformationService) controller.getServiceByName(HostInformationService.SERVICE_NAME);

        // Build endpoint URLs for available daemons
        // -- build-tv-daemon
        if (controller.isComponentEnabled(ComponentType.INTERVAL_DAEMON.COMPONENT_NAME))
        {
            long launcherClientPort = controller.getSettings().getLong(ComponentType.INTERVAL_DAEMON.SETTING_KEY_PORT);
            intervalServiceDashboardEndpointUrl = "http://localhost:" + launcherClientPort + "/" + ComponentType.INTERVAL_DAEMON.TOP_LEVEL_PATH + "/dashboards/urls/get";
        }
        else
        {
            intervalServiceDashboardEndpointUrl = null;
        }

        // -- led-daemon
        if (controller.isComponentEnabled(ComponentType.LED_DAEMON.COMPONENT_NAME))
        {
            long ledDaemonPort = controller.getSettings().getLong(ComponentType.LED_DAEMON.SETTING_KEY_PORT);
            ledDaemonPatternEndpointUrl = "http://localhost:" + ledDaemonPort + "/" + ComponentType.LED_DAEMON.TOP_LEVEL_PATH +"/leds/get";
        }
        else
        {
            ledDaemonPatternEndpointUrl = null;
        }

        // -- system-daemon
        if (controller.isComponentEnabled(ComponentType.SYSTEM_DAEMON.COMPONENT_NAME))
        {
            long systemDaemonPort = controller.getSettings().getLong(ComponentType.SYSTEM_DAEMON.SETTING_KEY_PORT);
            systemDaemonStatsEndpointUrl = "http://localhost:" + systemDaemonPort + "/" + ComponentType.SYSTEM_DAEMON.TOP_LEVEL_PATH + "/stats";
            systemDaemonScreenGetEndpointUrl = "http://localhost:" + systemDaemonPort + "/" + ComponentType.SYSTEM_DAEMON.TOP_LEVEL_PATH + "/screen/get";
        }
        else
        {
            systemDaemonStatsEndpointUrl = null;
            systemDaemonScreenGetEndpointUrl = null;
        }
    }

    @Override
    public void run()
    {
        LOG.debug("Waiting for controller to finish loading...");
        controller.waitForState(ControllerState.RUNNING);

        LOG.debug("Started...");

        long frequency = statsForwarderService.getFrequency();
        boolean shouldSendInfo = true;

        while (!isExit())
        {
            // Initially we will send our info to the endpoint, as a way to register with them, unless dashboards changes
            if (shouldSendInfo)
            {
                shouldSendInfo = !sendInfo();
            }
            else
            {
                // Send update to endpoint
                shouldSendInfo = !sendUpdate("online", true);
            }

            // Sleep...
            try
            {
                Thread.sleep(frequency);
            }
            catch (InterruptedException e) { }
        }

        // Send an update we're shutting down...
        LOG.debug("Sending shutdown update...");

        sendUpdate("shutdown", false);

        LOG.debug("Thread finished");
    }

    private boolean sendInfo()
    {
        LOG.debug("Building info message...");

        JSONObject request;

        // Build info request
        try
        {
            // Fetch Jira dashboards
            JSONArray dashboardUrls = fetchDashboardUrl(true);
            // -- Update cached value
            cacheDashboardUrls = dashboardUrls;

            // Build info object
            request = new JSONObject();

            request.put("uuid", instanceIdentityService.getInstanceUuid().toString());
            request.put("title", instanceIdentityService.getTitle());
            request.put("hostname", hostInformationService.getHostname());
            request.put("port", hostInformationService.getRestPort());
            request.put("auth", authProviderService.getAuthToken());
            request.put("version", versionService.getVersion());
            request.put("dashboards", dashboardUrls);
        }
        catch (Exception e)
        {
            LOG.error("Failed to build info message", e);
            return false;
        }

        // Send info request
        String endpointUrlInfo = statsForwarderService.getEndpointUrlInfo();
        LOG.debug("Sending info... - url: {}", endpointUrlInfo);

        try
        {
            // Send to endpoint
            RestClient restClient = new RestClient(STATS_FORWARDER_USER_AGENT, -1);
            HttpResponse httpResponse = restClient.executePost(endpointUrlInfo, request);

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            if (statusCode != 200)
            {
                LOG.warn("Failed to send info, unexpected HTTP status code - status code: {}, url: {}", statusCode, endpointUrlInfo);
                return false;
            }
            else
            {
                LOG.debug("Successfully sent info");
                return true;
            }
        }
        catch (IOException e)
        {
            LOG.error("Failed to connect to REST endpoint (sending info) - url: {}", endpointUrlInfo);
            return false;
        }
        catch (Exception e)
        {
            LOG.error("Failed to send info to REST endpoint", e);
            return false;
        }
    }

    private boolean sendUpdate(String status, boolean fetchExternalData)
    {
        String endpointUrlUpdate = statsForwarderService.getEndpointUrlUpdate();

        LOG.debug("Sending update... - url: {}", endpointUrlUpdate);

        try
        {
            // Fetch latest dashboards; we'll abort if it has changed, so that we can resend dashboards
            JSONArray dashboardUrls = fetchDashboardUrl(fetchExternalData);

            if  (   ((dashboardUrls != null || cacheDashboardUrls != null) && (dashboardUrls == null || cacheDashboardUrls == null)) ||
                    (dashboardUrls != null && !dashboardUrls.equals(cacheDashboardUrls))
                )
            {
                LOG.info("Dashboards have changed, aborting update and re-sending info...");
                return false;
            }

            // Fetch latest stats
            JSONArray jsonArrayStats = fetchStatistics(fetchExternalData);

            if (jsonArrayStats == null) {
                jsonArrayStats = new JSONArray();
            }

            // Fetch latest build indicator
            String buildIndicator = fetchBuildIndicator(fetchExternalData);

            if (buildIndicator == null) {
                buildIndicator = "shutdown";
            }

            // Fetch screen state
            Boolean screenState = fetchScreenState(fetchExternalData);

            if (screenState == null) {
                screenState = false;
            }

            // Build update packet object
            JSONObject response = new JSONObject();

            response.put("uuid", instanceIdentityService.getInstanceUuid().toString());
            response.put("status", status);
            response.put("buildIndicator", buildIndicator);
            response.put("metrics", jsonArrayStats);
            response.put("screen", screenState);

            // Send to endpoint
            RestClient restClient = new RestClient(STATS_FORWARDER_USER_AGENT, -1);
            restClient.executePost(endpointUrlUpdate, response);

            LOG.debug("Successfully sent update");

            return true;
        }
        catch (IOException e)
        {
            LOG.error("Failed to connect to REST endpoint (sending update) - url: {}", endpointUrlUpdate);
            return false;
        }
        catch (Exception e)
        {
            LOG.error("Failed to send system REST update", e);
            return false;
        }
    }

    private String fetchBuildIndicator(boolean fetchExternalData)
    {
        String pattern = (String) fetchJsonObjectFromUrl(ledDaemonPatternEndpointUrl, new String[]{"current", "pattern"}, fetchExternalData);

        LOG.debug("Current build indicator retrieved - pattern: {}", pattern);

        return pattern;
    }

    private JSONArray fetchStatistics(boolean fetchExternalData)
    {
        JSONArray response = (JSONArray) fetchDataFromUrl(systemDaemonStatsEndpointUrl, fetchExternalData);
        LOG.debug("Retrieved statistics - metrics: {}", response != null ? response.size() : 0);

        return response;
    }

    private JSONArray fetchDashboardUrl(boolean fetchExternalData)
    {
        // WARNING: THE FOLLOWING IS SENSITIVE CODE AND COULD BE USED AS AN ATTACK VECTOR TO
        // COMPROMISE NON-PUBLIC URLs, SUCH AS JIRA WALLBOARDS, WITH PASSWORDS

        JSONArray response = (JSONArray) fetchJsonObjectFromUrl(intervalServiceDashboardEndpointUrl, new String[]{"dashboards"}, fetchExternalData);
        LOG.debug("Retrieved dashboard - url: {}, size: {}", intervalServiceDashboardEndpointUrl, response != null ? response.size() : 0);

        // Re-wrap with only public URLs
        JSONArray sanitisedResponse = new JSONArray();

        if (response != null && response.size() > 0)
        {
            Object rawItem;
            JSONObject dashboard;
            String sanitisedDashboardUrl;

            for (int i = 0; i < response.size(); i++)
            {
                rawItem = response.get(i);
                dashboard = (JSONObject) rawItem;

                // Convert to sanitised dashboard and add
                sanitisedDashboardUrl = (String) dashboard.get("public_url");
                sanitisedResponse.add(sanitisedDashboardUrl);
            }
        }

        return sanitisedResponse;
    }

    public Boolean fetchScreenState(boolean fetchExternalData)
    {
        Boolean screenState = (Boolean) fetchJsonObjectFromUrl(systemDaemonScreenGetEndpointUrl, new String[]{ "on" }, fetchExternalData);
        LOG.debug("Retrieved screen state - on: {}", screenState);

        return screenState;
    }

    private Object fetchJsonObjectFromUrl(String url, String[] path, boolean fetchExternalData)
    {
        Object response = fetchDataFromUrl(url, fetchExternalData);

        if (response != null)
        {
            // Check object is correct type
            if (!(response instanceof JSONObject))
            {
                LOG.warn("Incorrect type returned, expected JSONObject - url: {}, class: {}", url, response.getClass().getName());
                return null;
            }

            // Check key present
            if (path != null)
            {
                JSONObject jsonRoot = (JSONObject) response;
                Object value = JsonUtil.getNestedNode(jsonRoot, path);

                if (value == null)
                {
                    LOG.warn("Value not present in JSON response - url: {}, path: {}", url, path);
                }

                return value;
            }
            else
            {
                return response;
            }
        }
        else
        {
            return null;
        }
    }

    private Object fetchDataFromUrl(String url, boolean fetchExternalData)
    {
        // Check we're allowed to fetch external data
        if (!fetchExternalData)
        {
            LOG.debug("Ignored request to fetch data, external flag set is false - url: {}", url);
            return null;
        }
        // Check daemon is available
        else if (url == null)
        {
            LOG.debug("Ignored request to fetch data, component unavailable");
            return null;
        }

        // Attempt to fetch data
        try
        {
            LOG.debug("Fetching data - url: {}", url);

            RestClient restClient = new RestClient(STATS_FORWARDER_USER_AGENT, -1);
            return restClient.executeGet(url);
        }
        catch (IOException e)
        {
            LOG.error("Failed to connect to component - url: {}", url);
            return null;
        }
        catch (Exception e)
        {
            LOG.error("Failed to retrieve data - url: " + url, e);
            return null;
        }
    }

}
