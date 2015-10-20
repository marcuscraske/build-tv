package com.limpygnome.daemon.remote.service.thread;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.remote.service.StatsForwarderService;
import com.limpygnome.daemon.remote.service.InstanceIdentityService;
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

    public StatsForwarderThread(Controller controller, StatsForwarderService statsForwarderService)
    {
        this.statsForwarderService = statsForwarderService;
        this.instanceIdentityService = (InstanceIdentityService) controller.getServiceByName(InstanceIdentityService.SERVICE_NAME);
    }

    @Override
    public void run()
    {
        // Initially we will send our info to the endpoint, as a way to register with them...

        long frequency = statsForwarderService.getFrequency();

        while (!isExit())
        {
            // Send update to endpoint
            sendUpdate("online");

            // Sleep...
            try
            {
                Thread.sleep(frequency);
            }
            catch (InterruptedException e) { }
        }

        // Send an update we're shutting down...
        sendUpdate("shutdown");
    }

    private void sendInfo()
    {
        JSONObject request = new JSONObject();
        request.put("uuid", instanceIdentityService.getInstanceUuid());
        request.put("title", instanceIdentityService.getTitle());
        request.put("hostname", null);
        request.put("port", 123);
        request.put("auth", null);
        request.put("version", null);
    }

    private void sendUpdate(String status)
    {
        // Fetch latest stats
        JSONArray jsonArrayStats = fetchStatistics();

        // Fetch latest build indicator
        String buildIndicator = fetchBuildIndicator();

        // Build update packet object
        JSONObject request = new JSONObject();
        request.put("uuid", instanceIdentityService.getInstanceUuid());
        request.put("status", status);
        request.put("buildIndicator", buildIndicator);
        request.put("metrics", jsonArrayStats);

        // Send to endpoint
        RestClient restClient = new RestClient(STATS_FORWARDER_USER_AGENT, -1);

        try
        {
            restClient.executePost(statsForwarderService.getEndpointUrlUpdate(), request);
        }
        catch (Exception e)
        {
            LOG.error("Failed to send system REST update", e);
        }
    }

    private String fetchBuildIndicator()
    {
        // TODO: unstub this...
        return "build-ok";
    }

    private JSONArray fetchStatistics()
    {
        // TODO: unstub this...
        return null;
    }

}
