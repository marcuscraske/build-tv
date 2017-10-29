package com.limpygnome.daemon.buildstatus.jenkins;

import com.limpygnome.daemon.api.LedPattern;
import com.limpygnome.daemon.buildstatus.model.JenkinsHostUpdateResult;
import com.limpygnome.daemon.buildstatus.model.JenkinsJob;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * Used to hold information and poll a Jenkins host.
 */
public class JenkinsHost
{
    private static final Logger LOG = LogManager.getLogger(JenkinsHost.class);

    private String name;
    private String baseUrl;
    private String finalUrl;
    private List<String> jobs;

    public JenkinsHost(String name, String baseUrl, List<String> jobs)
    {
        // Cleanup params
        if (baseUrl.length() > 1 && baseUrl.endsWith("/"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // Assign params
        this.name = name;
        this.baseUrl = baseUrl;
        this.jobs = jobs;

        // Build final (API) url
        this.finalUrl = this.baseUrl + "/api/json";
    }

    public JenkinsHostUpdateResult update(RestClient restClient)
    {
        try
        {
            boolean jobsEmpty = jobs.isEmpty();

            // Fetch available jobs
            LOG.debug("Polling Jenkins job... - name: {}", name);

            JSONObject root = restClient.executeJson(finalUrl);

            // Fetch all jobs
            JSONArray rawJobsArray = (JSONArray) root.get("jobs");

            JSONObject rawJob;
            String projectName;
            String status;

            JenkinsHostUpdateResult result = new JenkinsHostUpdateResult();
            JenkinsJob jenkinsJob;

            for (int i = 0; i < rawJobsArray.size(); i++)
            {
                rawJob = (JSONObject) rawJobsArray.get(i);

                // Fetch project information
                projectName = (String) rawJob.get("name");
                status = (String) rawJob.get("color");

                // Check if project within list
                if (jobsEmpty || jobs.contains(projectName))
                {
                    // Handle status to get led pattern
                    LedPattern ledPattern;

                    switch (status)
                    {
                        case "blue_anime":
                        case "yellow_anime":
                        case "red_anime":
                        case "aborted_anime":
                            ledPattern = LedPattern.BUILD_PROGRESS;
                            result.addAffectedJob(projectName);
                            break;
                        case "blue":
                        case "notbuilt":
                        case "disabled":
                            ledPattern = LedPattern.BUILD_OK;
                            break;
                        case "red":
                            ledPattern = LedPattern.BUILD_FAILURE;
                            result.addAffectedJob(projectName);
                            break;
                        case "yellow":
                        case "aborted":
                            ledPattern = LedPattern.BUILD_UNSTABLE;
                            result.addAffectedJob(projectName);
                            break;
                        default:
                            LOG.warn("Unknown build status/colour - name: {}, project: {}, status: {}",
                                    name, projectName, status);

                            ledPattern = LedPattern.BUILD_UNKNOWN;
                            break;
                    }

                    // Add to jobs handled
                    jenkinsJob = new JenkinsJob(projectName, ledPattern);
                    result.addJob(jenkinsJob);

                    // Check if pattern is higher than highest found thus far
                    if (ledPattern.PRIORITY > result.getLedPattern().PRIORITY)
                    {
                        result.setLedPattern(ledPattern);
                    }
                }
            }

            LOG.debug("Jenkins host status - name: {}, pattern: {}", name, result.getLedPattern().PATTERN);

            return result;
        }
        catch (Exception e)
        {
            LOG.error("Failed to poll Jenkins host - name: {}, url: {}", name, finalUrl, e);

            return new JenkinsHostUpdateResult(LedPattern.JENKINS_UNAVAILABLE);
        }
    }

}
