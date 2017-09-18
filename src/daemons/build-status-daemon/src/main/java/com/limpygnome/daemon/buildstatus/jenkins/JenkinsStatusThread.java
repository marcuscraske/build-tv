package com.limpygnome.daemon.buildstatus.jenkins;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ControllerState;
import com.limpygnome.daemon.api.LedPattern;
import com.limpygnome.daemon.buildstatus.model.JenkinsHostUpdateResult;
import com.limpygnome.daemon.api.Notification;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.common.rest.client.LedClient;
import com.limpygnome.daemon.common.rest.client.NotificationClient;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Checks build status against list of projects on Jenkins.
 */
public class JenkinsStatusThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(JenkinsStatusThread.class);

    private static final String LED_SOURCE_NAME = "jenkins-status";

    /* The global configuration file for Jenkins. */
    private static final String JENKINS_SETTINGS_FILENAME = "jenkins.json";

    /* The HTTP user agent for any requests made by the build TV to Jenkins. */
    private static final String USER_AGENT = "build-tv-jenkins-poller";

    /* The source name for notifications sent to the notification service. */
    private static final String NOTIFICATION_SOURCE_NAME = "build-tv-jenkins";

    private Controller controller;
    private long pollRateMs;
    private int bufferSizeBytes;

    private LedClient ledClient;
    private LedPattern lastLedPattern;
    private NotificationClient notificationClient;
    private RestClient restClient;

    private JenkinsHost[] jenkinsHosts;

    /* Latest result from polling hosts. */
    private JenkinsHostUpdateResult result;


    public JenkinsStatusThread(Controller controller)
    {
        this.controller = controller;

        // Load configuration from file
        loadConfigurationFromFile(controller);

        // Setup LED client
        this.ledClient = new LedClient(controller, LED_SOURCE_NAME);

        // Set initial LED pattern
        try
        {
            ledClient.changeLedPattern(LedPattern.BUILD_UNKNOWN);
        }
        catch (Exception e)
        {
            LOG.error("Failed to set initial LED pattern", e);
        }

        // Setup notification client
        this.notificationClient = new NotificationClient(controller, NOTIFICATION_SOURCE_NAME);

        // Setup REST client
        this.restClient = new RestClient(USER_AGENT);

        // Set last pattern to null; will be used to track last led pattern for deciding if to update notification
        this.lastLedPattern = null;
    }

    private void loadConfigurationFromFile(Controller controller)
    {
        // Retrieve file instance of Jenkins file
        File file = controller.findConfigFile(JENKINS_SETTINGS_FILENAME);

        try
        {
            // Load JSON from file
            JSONParser jsonParser = new JSONParser();
            JSONObject root = (JSONObject) jsonParser.parse(new FileReader(file));

            // Load mandatory configuration
            this.pollRateMs = (long) root.get("poll-rate-ms");
            this.bufferSizeBytes = (int) (long) root.get("max-buffer-bytes");

            LOG.debug("Poll rate: {}, max buffer bytes: {}", pollRateMs, bufferSizeBytes);

            // Parse JSON into hosts
            JSONArray rawHosts = (JSONArray) root.get("hosts");
            this.jenkinsHosts = parseHosts(rawHosts);
        }
        catch (Exception e)
        {
            String absPath = file.getAbsolutePath();
            LOG.error("Failed to load Jenkins hosts file - path: {}", absPath, e);
            throw new RuntimeException("Failed to load Jenkins hosts file - path: " + absPath);
        }
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

        // Check we have hosts, else terminate...
        if (jenkinsHosts == null || jenkinsHosts.length == 0)
        {
            LOG.warn("No Jenkins hosts found, exiting thread...");
            return;
        }

        // Wait until all services running...
        controller.waitForState(ControllerState.RUNNING);

        // Run until thread exits, polling Jenkins for status and updating pattern source
        while (!isExit())
        {
            try
            {
                // Poll Jenkins
                result = pollHosts();

                // Set LED pattern to highest found from hosts
                try
                {
                    ledClient.changeLedPattern(result.getLedPattern());
                }
                catch (Exception e)
                {
                    LOG.error("Failed to update LED pattern", e);
                }

                // Display notification for certain LED patterns
                try
                {
                    updateNotificationFromJenkinsResult(result);
                }
                catch (Exception e)
                {
                    LOG.error("Failed to update current notification", e);
                }

                // Wait a while...
                Thread.sleep(pollRateMs);
            }
            catch (InterruptedException e)
            {
                LOG.error("Exception during Jenkins status thread", e);
            }
        }

        // Remove our pattern from LED time service
        ledClient.removeSource();
    }

    private void updateNotificationFromJenkinsResult(JenkinsHostUpdateResult hostsResult)
    {
        // TODO: probably shouldn't have magic values here, clean it up eventually using config with fallback default values
        LedPattern currentLedPattern = hostsResult.getLedPattern();

        if (this.lastLedPattern == null || this.lastLedPattern != currentLedPattern)
        {
            // Update last led pattern to avoid sending multiple notifications
            this.lastLedPattern = currentLedPattern;

            // Build text showing affected jobs
            String text;
            List<String> affectedJobs = hostsResult.getAffectedJobs();

            if (affectedJobs.size() > 4)
            {
                text = affectedJobs.size() + " jobs affected";
            }
            else if (!affectedJobs.isEmpty())
            {
                StringBuilder buffer = new StringBuilder();

                for (String job : affectedJobs)
                {
                    buffer.append(job).append("\n");
                }
                buffer.delete(buffer.length()-1, buffer.length());

                text = buffer.toString();
            }
            else
            {
                text = null;
            }

            // Build notification
            Notification notification;

            switch (hostsResult.getLedPattern())
            {
                case BUILD_FAILURE:
                    notification = new Notification("build failure", text, 60000, "build-failure", 10);
                    break;
                case BUILD_OK:
                    notification = new Notification("build success", text, 10000, "build-ok", 10);
                    break;
                case BUILD_PROGRESS:
                    notification = new Notification("build in progress...", text, 10000, "build-progress", 10);
                    break;
                case BUILD_UNSTABLE:
                    notification = new Notification("build unstable", text, 60000, "build-unstable", 10);
                    break;
                case JENKINS_UNAVAILABLE:
                    notification = new Notification("Jenkins offline", text, 0, "jenkins-unavailable", 10);
                    break;
                default:
                    notification = null;
                    break;
            }

            // Update via service
            if (notification != null)
            {
                notificationClient.updateNotification(notification);
            }
            else
            {
                notificationClient.removeNotification();
            }
        }
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
            hostsResult.mergeJobs(result);
        }

        // Set overall LED pattern to highest found
        hostsResult.setLedPattern(highestLedPattern);

        return hostsResult;
    }

    public JenkinsHostUpdateResult getLatestResult()
    {
        return result;
    }

}
