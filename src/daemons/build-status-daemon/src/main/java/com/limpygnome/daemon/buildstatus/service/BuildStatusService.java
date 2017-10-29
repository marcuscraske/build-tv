package com.limpygnome.daemon.buildstatus.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.buildstatus.model.JenkinsHostUpdateResult;
import com.limpygnome.daemon.buildstatus.model.JenkinsJob;
import com.limpygnome.daemon.common.rest.RestRequest;
import com.limpygnome.daemon.common.rest.RestResponse;
import com.limpygnome.daemon.api.RestServiceHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A service for retrieving the build status of Jenkins projects (as a whole).
 */
public class BuildStatusService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(BuildStatusService.class);

    public static final String SERVICE_NAME = "build-status";

    private JenkinsService jenkinsService;

    @Override
    public void start(Controller controller)
    {
        // Retrieve Jenkins service
        jenkinsService = (JenkinsService) controller.getServiceByName(JenkinsService.SERVICE_NAME);
    }

    @Override
    public void stop(Controller controller)
    {
        jenkinsService = null;
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        if (restRequest.isPathMatch(new String[]{ "build-status-daemon", "status", "get" }))
        {
            return handleRequest_statusGet(restRequest, restResponse);
        }

        return false;
    }

    private boolean handleRequest_statusGet(RestRequest restRequest, RestResponse restResponse)
    {
        // Retrieve projects with their current status
        JenkinsHostUpdateResult result = jenkinsService.getLatestResult();

        // Build response
        JSONObject root = new JSONObject();

        // -- Add status of each project/job
        JSONArray jobs = new JSONArray();

        if (result != null)
        {
            JSONObject rawJsonJob;
            for (JenkinsJob jenkinsJob : result.getJobs())
            {
                rawJsonJob = new JSONObject();
                rawJsonJob.put("name", jenkinsJob.getName());
                rawJsonJob.put("status", jenkinsJob.getStatus().name());

                jobs.add(rawJsonJob);
            }
        }
        root.put("jobs", jobs);

        // Write response
        restResponse.writeJsonResponseIgnoreExceptions(root);

        return true;
    }

}
