package com.limpygnome.daemon.buildtv.jenkins;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Settings;
import com.limpygnome.daemon.buildtv.led.pattern.LedPattern;
import com.limpygnome.daemon.buildtv.led.pattern.source.PatternSource;
import com.limpygnome.daemon.buildtv.model.JenkinsHostUpdateResult;
import com.limpygnome.daemon.buildtv.model.Notification;
import com.limpygnome.daemon.buildtv.service.LedTimeService;
import com.limpygnome.daemon.buildtv.service.NotificationService;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

/**
 * Checks build status against list of projects on Jenkins.
 */
public class JenkinsStatusThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(JenkinsStatusThread.class);

    private LedTimeService ledTimeService;
    private NotificationService notificationService;
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
        this.patternSource = new PatternSource("Jenkins Status", LedPattern.BUILD_UNKNOWN, 1);

        // Fetch LED time service for later
        this.ledTimeService = (LedTimeService) controller.getServiceByName("led-time");

        // Fetch notifications service
        this.notificationService = (NotificationService) controller.getServiceByName(NotificationService.SERVICE_NAME);
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
        JenkinsHostUpdateResult hostsResult;
        Notification notification;

        while (!isExit())
        {
            try
            {
                // Poll Jenkins
                hostsResult = pollHosts();

                // Set LED pattern to highest found from hosts
                patternSource.setCurrentLedPattern(hostsResult.getLedPattern());

                // Display notification for certain LED patterns
                updateNotificationFromJenkinsResult(hostsResult);

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

    private void updateNotificationFromJenkinsResult(JenkinsHostUpdateResult hostsResult)
    {
        // Build notification
        Notification notification;

        // TODO: probably shouldn't have magic values here, clean it up eventually using config with fallback default values
        switch (hostsResult.getLedPattern())
        {
            case BUILD_FAILURE:
                notification = new Notification("build failure", null, 60000, Color.decode("#CC3300"));
                break;
            case BUILD_OK:
                notification = new Notification("build success", null, 30000, Color.decode("#339933"));
                break;
            case BUILD_PROGRESS:
                notification = new Notification("build in progress...", null, 30000, Color.decode("#003D99"));
                break;
            case BUILD_UNSTABLE:
                notification = new Notification("build unstable", null, 60000, Color.decode("#FF9933"));
                break;
            case JENKINS_UNAVAILABLE:
                notification = new Notification("Jenkins offline", null, 0, Color.decode("#CC0000"));
                break;
            default:
                notification = null;
                break;
        }

        // Build text showing affected jobs
        List<String> affectedJobs = hostsResult.getAffectedJobs();

        if (affectedJobs.size() > 4)
        {
            notification.setText(affectedJobs.size() + " jobs affected");
        }
        else if (!affectedJobs.isEmpty())
        {
            StringBuilder buffer = new StringBuilder();

            for (String job : affectedJobs)
            {
                buffer.append(job).append("\n");
            }
            buffer.delete(buffer.length()-1, buffer.length());

            notification.setText(buffer.toString());
        }

        // Update via service
        notificationService.updateCurrentNotification(notification);
    }

    private JenkinsHostUpdateResult pollHosts()
    {
        JenkinsHostUpdateResult hostsResult = new JenkinsHostUpdateResult();
        LedPattern highestLedPattern = LedPattern.BUILD_UNKNOWN;

        JenkinsHostUpdateResult result;
        LedPattern resultLedPattern;

        // Get status of each job and keep highest priority LED pattern
        for (JenkinsHost jenkinsHost : jenkinsHosts)
        {
            result = jenkinsHost.update(restClient);
            resultLedPattern = result.getLedPattern();

            // Determine if host's pattern has highest priority
            if (resultLedPattern.PRIORITY > highestLedPattern.PRIORITY)
            {
                highestLedPattern = resultLedPattern;
            }

            // Merge affected jobs into overall result
            hostsResult.mergeAffectedJobs(result);
        }

        // Set overall LED pattern to highest found
        hostsResult.setLedPattern(highestLedPattern);

        return hostsResult;
    }

}
