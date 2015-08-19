package com.limpygnome.daemon.buildtv.jenkins;

import com.limpygnome.daemon.buildtv.led.LedDisplayPatterns;
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

    public LedDisplayPatterns update(RestClient restClient)
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
            String project;
            String status;

            LedDisplayPatterns ledPatternHighest = LedDisplayPatterns.BUILD_UNKNOWN;

            for (int i = 0; i < rawJobsArray.size(); i++)
            {
                rawJob = (JSONObject) rawJobsArray.get(i);

                // Fetch project information
                project = (String) rawJob.get("name");
                status = (String) rawJob.get("color");

                // Check if project within list
                if (jobsEmpty || jobs.contains(project))
                {
                    // Handle status to get led pattern
                    LedDisplayPatterns ledPattern;

                    switch (status)
                    {
                        case "blue_anime":
                        case "yellow_anime":
                        case "red_anime":
                        case "aborted_anime":
                            ledPattern = LedDisplayPatterns.BUILD_PROGRESS;
                            break;
                        case "blue":
                        case "notbuilt":
                            ledPattern = LedDisplayPatterns.BUILD_OK;
                            break;
                        case "red":
                            ledPattern = LedDisplayPatterns.BUILD_FAILURE;
                            break;
                        case "yellow":
                        case "aborted":
                            ledPattern = LedDisplayPatterns.BUILD_UNSTABLE;
                            break;
                        default:
                            LOG.warn("Unknown build status/colour - name: {}, project: {}, status: {}",
                                    name, project, status);

                            ledPattern = LedDisplayPatterns.BUILD_UNKNOWN;
                            break;
                    }

                    // Check if pattern is higher than highest found thus far
                    if (ledPattern.PRIORITY > ledPatternHighest.PRIORITY)
                    {
                        ledPatternHighest = ledPattern;
                    }
                }
            }

            LOG.debug("Jenkins host status - name: {}, pattern: {}", name, ledPatternHighest.PATTERN);

            return ledPatternHighest;
        }
        catch (Exception e)
        {
            LOG.error("Failed to poll Jenkins host - name: {}, url: {}", name, finalUrl, e);

            return LedDisplayPatterns.JENKINS_UNAVAILABLE;
        }
    }

}
