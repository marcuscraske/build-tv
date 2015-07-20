package com.limpygnome.daemon.buildtv.jenkins;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.buildtv.led.LedPattern;
import com.limpygnome.daemon.buildtv.led.PatternSource;
import com.limpygnome.daemon.buildtv.service.LedTimeService;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.util.StreamUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Checks build status against list of projects on Jenkins.
 */
public class JenkinsStatusThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(JenkinsStatusThread.class);

    private LedTimeService ledTimeService;
    private long jenkinsPollRate;
    private String[] jenkinsJobStatusUrls;
    private PatternSource patternSource;

    public JenkinsStatusThread(Controller controller, long jenkinsPollRate, String[] jenkinsJobStatusUrls)
    {
        this.jenkinsJobStatusUrls = jenkinsJobStatusUrls;
        this.jenkinsPollRate = jenkinsPollRate;
        this.patternSource = new PatternSource("Jenkins Status", LedPattern.BUILD_UNKNOWN, 1);

        // Fetch LED time service and add our pattern source
        this.ledTimeService = (LedTimeService) controller.getServiceByName("led-time");
    }

    @Override
    public void run()
    {
        // Add our pattern to LED time service
        ledTimeService.addPatternSource(patternSource);

        // Run until thread exits, polling Jenkins for status and updating pattern source
        LedPattern ledPattern;
        while (!isExit())
        {
            try
            {
                // Poll Jenkins
                ledPattern = pollJenkins();
                patternSource.setCurrentLedPattern(ledPattern);

                // Wait a while...
                Thread.sleep(jenkinsPollRate);
            }
            catch (InterruptedException e)
            {
                LOG.error("Exception during Jenkins status thread", e);
            }
        }

        // Remove our pattern from LED time service
        ledTimeService.removePatternSource(patternSource);
    }

    private LedPattern pollJenkins()
    {
        LedPattern highest = LedPattern.BUILD_UNKNOWN;
        LedPattern current;

        // Get status of each job and keep highest priority LED pattern
        for (String job : jenkinsJobStatusUrls)
        {
            current = pollJenkinsJob(job);

            if (current.PRIORITY > highest.PRIORITY)
            {
                highest = current;
            }
        }

        return highest;
    }

    private LedPattern pollJenkinsJob(String job)
    {
        LOG.debug("Polling job... - url: {}", job);

        try
        {
            // Build request for JSON status of job
            HttpClient httpClient = HttpClients.createMinimal();

            HttpGet httpGet = new HttpGet(job);
            httpGet.setHeader("User-Agent", "Jenkins Status Thread");

            // Execute and read response
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String response = StreamUtil.readInputStream(httpResponse.getEntity().getContent(), 64000);

            JSONObject jsonRoot;

            try
            {
                JSONParser jsonParser = new JSONParser();
                jsonRoot = (JSONObject) jsonParser.parse(response);
            }
            catch (ParseException e)
            {
                throw new RuntimeException("Failed to parse content [" + response.length() + " chars]: " + response, e);
            }

            String result = (String) jsonRoot.get("result");

            LedPattern ledPattern;

            if (result == null)
            {
                ledPattern = LedPattern.BUILD_PROGRESS;
            }
            else
            {
                switch (result)
                {
                    case "FAILURE":
                        ledPattern = LedPattern.BUILD_FAILURE;
                        break;
                    case "UNSTABLE":
                    case "ABORTED":
                        ledPattern = LedPattern.BUILD_UNSTABLE;
                        break;
                    case "SUCCESS":
                        ledPattern = LedPattern.BUILD_OK;
                        break;
                    default:
                        ledPattern = LedPattern.BUILD_UNKNOWN;
                        break;
                }
            }

            LOG.debug("Job status - url: {}, status: {}", job, ledPattern.PATTERN);

            return ledPattern;
        }
        catch (Exception e)
        {
            LOG.error("Failed to poll Jenkins job", e);
            LOG.error("Failed polling job - url: {}", job);
            return LedPattern.JENKINS_UNAVAILABLE;
        }
    }



}
