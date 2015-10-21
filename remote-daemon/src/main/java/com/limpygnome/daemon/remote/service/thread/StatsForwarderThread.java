package com.limpygnome.daemon.remote.service.thread;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ControllerState;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.remote.service.HostInformationService;
import com.limpygnome.daemon.remote.service.StatsForwarderService;
import com.limpygnome.daemon.remote.service.InstanceIdentityService;
import com.limpygnome.daemon.remote.service.VersionService;
import com.limpygnome.daemon.remote.service.auth.AuthProviderService;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by limpygnome on 20/10/15.
 */
public class StatsForwarderThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(StatsForwarderThread.class);

    private static final String STATS_FORWARDER_USER_AGENT = "stats-forwarder";

    private Controller controller;
    private StatsForwarderService statsForwarderService;
    private InstanceIdentityService instanceIdentityService;
    private AuthProviderService authProviderService;
    private VersionService versionService;
    private HostInformationService hostInformationService;

    private String LedDaemonPatternEndpointUrl;
    private String systemDaemonStatsEndpointUrl;

    public StatsForwarderThread(Controller controller, StatsForwarderService statsForwarderService)
    {
        this.controller = controller;
        this.statsForwarderService = statsForwarderService;
        this.instanceIdentityService = (InstanceIdentityService) controller.getServiceByName(InstanceIdentityService.SERVICE_NAME);
        this.authProviderService = (AuthProviderService) controller.getServiceByName(AuthProviderService.SERVICE_NAME);
        this.versionService = (VersionService) controller.getServiceByName(VersionService.SERVICE_NAME);
        this.hostInformationService = (HostInformationService) controller.getServiceByName(HostInformationService.SERVICE_NAME);

        // Load daemon port settings
        long ledDaemonport = controller.getSettings().getLong("local-ports/led-daemon");
        long systemDaemonPort = controller.getSettings().getLong("local-ports/system-daemon");

        // Build endpoint URLs
        LedDaemonPatternEndpointUrl = "http://localhost:" + ledDaemonport + "/led-daemon/leds/get";
        systemDaemonStatsEndpointUrl = "http://localhost:" + systemDaemonPort + "/system-daemon/stats";
    }

    @Override
    public void run()
    {
        LOG.debug("Waiting for controller to finish loading...");
        controller.waitForState(ControllerState.RUNNING);

        LOG.debug("Started...");

        long frequency = statsForwarderService.getFrequency();
        boolean infoSent = false;

        while (!isExit())
        {
            // Initially we will send our info to the endpoint, as a way to register with them...
            if (!infoSent)
            {
                infoSent = sendInfo();
            }
            else
            {
                // Send update to endpoint
                sendUpdate("online", true);
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
        LOG.debug("Sending info...");

        try
        {
            // Build info object
            JSONObject request = new JSONObject();

            request.put("uuid", instanceIdentityService.getInstanceUuid().toString());
            request.put("title", instanceIdentityService.getTitle());
            request.put("hostname", hostInformationService.getHostname());
            request.put("port", hostInformationService.getRestPort());
            request.put("auth", authProviderService.getAuthToken());
            request.put("version", versionService.getVersion());

            // Send to endpoint
            RestClient restClient = new RestClient(STATS_FORWARDER_USER_AGENT, -1);
            restClient.executePost(statsForwarderService.getEndpointUrlInfo(), request);

            LOG.debug("Successfully sent info");

            return true;
        }
        catch (Exception e)
        {
            LOG.error("Failed to send info to REST endpoint", e);
            return false;
        }
    }

    private void sendUpdate(String status, boolean fetchExternalData)
    {
        LOG.debug("Sending update...");

        try
        {
            // Fetch latest stats
            JSONArray jsonArrayStats = fetchStatistics(fetchExternalData);

            // Fetch latest build indicator
            String buildIndicator = fetchBuildIndicator(fetchExternalData);

            // Build update packet object
            JSONObject request = new JSONObject();
            request.put("uuid", instanceIdentityService.getInstanceUuid().toString());
            request.put("status", status);
            request.put("buildIndicator", buildIndicator);
            request.put("metrics", jsonArrayStats);

            // Send to endpoint
            RestClient restClient = new RestClient(STATS_FORWARDER_USER_AGENT, -1);
            restClient.executePost(statsForwarderService.getEndpointUrlUpdate(), request);

            LOG.debug("Successfully sent update");
        }
        catch (Exception e)
        {
            LOG.error("Failed to send system REST update", e);
        }
    }

    private String fetchBuildIndicator(boolean fetchExternalData)
    {
        if (!fetchExternalData)
        {
            return null;
        }

        try
        {
            LOG.debug("Fetching build indicator - url: {}", LedDaemonPatternEndpointUrl);

            RestClient restClient = new RestClient(STATS_FORWARDER_USER_AGENT, -1);
            JSONObject response = restClient.executeJson(LedDaemonPatternEndpointUrl);
            String pattern = (String) ((JSONObject) response.get("current")).get("pattern");

            LOG.debug("Current build indicator retrieved - pattern: {}", pattern);

            return pattern;
        }
        catch (Exception e)
        {
            LOG.error("Failed to retrieve build indicator from LED daemon", e);
            return null;
        }
    }

    private JSONArray fetchStatistics(boolean fetchExternalData)
    {
        if (!fetchExternalData)
        {
            return null;
        }

        try
        {
            LOG.debug("Fetching statistics - url: {}", systemDaemonStatsEndpointUrl);

            RestClient restClient = new RestClient(STATS_FORWARDER_USER_AGENT, -1);
            JSONArray response = (JSONArray) restClient.executeGet(systemDaemonStatsEndpointUrl);

            LOG.debug("Retrieved statistics - metrics: {}", response.size());

            return response;
        }
        catch (Exception e)
        {
            LOG.error("Failed to retrieve statistics from system daemon", e);
            return null;
        }
    }

}
