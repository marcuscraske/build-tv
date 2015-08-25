package com.limpygnome.daemon.buildtv.jenkins;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Settings;
import com.limpygnome.daemon.buildtv.led.pattern.LedPatterns;
import com.limpygnome.daemon.buildtv.led.pattern.source.PatternSource;
import com.limpygnome.daemon.buildtv.service.LedTimeService;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedList;

/**
 * Checks build status against list of projects on Jenkins.
 */
public class JenkinsStatusThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(JenkinsStatusThread.class);

    private LedTimeService ledTimeService;
    private long pollRateMs;
    private PatternSource patternSource;

    private JenkinsHost[] jenkinsHosts;
    private RestClient restClient;

    public JenkinsStatusThread(Controller controller)
    {
        Settings settings = controller.getSettings();

        // Read global settings
        this.pollRateMs = settings.getLong("jenkins/poll-rate-ms");

        // Parse hosts to poll
        JSONArray rawHosts = settings.getJsonArray("jenkins/hosts");
        this.jenkinsHosts = parseHosts(rawHosts);

        // Setup REST client
        int bufferSizeBytes = settings.getInt("jenkins/max-buffer-bytes");
        String userAgent = settings.getString("jenkins/user-agent");

        this.restClient = new RestClient(userAgent, bufferSizeBytes);

        // Setup a new LED pattern source for this thread
        this.patternSource = new PatternSource("Jenkins Status", LedPatterns.BUILD_UNKNOWN, 1);

        // Fetch LED time service for later
        this.ledTimeService = (LedTimeService) controller.getServiceByName("led-time");
    }

    private JenkinsHost[] parseHosts(JSONArray rawHosts)
    {
        JenkinsHost[] jenkinsHosts = new JenkinsHost[rawHosts.size()];

        JSONObject rawHost;
        JSONArray rawHostJobs;
        JenkinsHost jenkinsHost;
        LinkedList<String> jobs;

        for (int i = 0; i < rawHosts.size(); i++)
        {
            rawHost = (JSONObject) rawHosts.get(i);

            // Parse jobs
            jobs = new LinkedList<>();

            if (rawHost.containsKey("jobs"))
            {
                rawHostJobs = (JSONArray) rawHost.get("jobs");

                for (int j = 0; j < rawHostJobs.size(); j++)
                {
                    jobs.add((String) rawHostJobs.get(j));
                }
            }

            // Parse host
            jenkinsHost = new JenkinsHost(
                    (String) rawHost.get("name"),
                    (String) rawHost.get("url"),
                    jobs
            );

            jenkinsHosts[i] = jenkinsHost;
        }

        return jenkinsHosts;
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName("Jenkins Status");

        // Add our pattern to LED time service
        ledTimeService.addPatternSource(patternSource);

        // Run until thread exits, polling Jenkins for status and updating pattern source
        LedPatterns ledPattern;

        while (!isExit())
        {
            try
            {
                // Poll Jenkins
                ledPattern = pollHosts();
                patternSource.setCurrentLedPattern(ledPattern);

                // Wait a while...
                Thread.sleep(pollRateMs);
            }
            catch (InterruptedException e)
            {
                LOG.error("Exception during Jenkins status thread", e);
            }
        }

        // Remove our pattern from LED time service
        ledTimeService.removePatternSource(patternSource);
    }

    private LedPatterns pollHosts()
    {
        LedPatterns highest = LedPatterns.BUILD_UNKNOWN;
        LedPatterns current;

        // Get status of each job and keep highest priority LED pattern
        for (JenkinsHost jenkinsHost : jenkinsHosts)
        {
            current = jenkinsHost.update(restClient);

            if (current.PRIORITY > highest.PRIORITY)
            {
                highest = current;
            }
        }

        return highest;
    }

}
