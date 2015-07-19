package com.limpygnome.daemon.buildtv.jenkins;

import com.limpygnome.daemon.buildtv.led.LedPattern;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.util.Streams;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.ConnectException;

/**
 * Checks build status against list of projects on Jenkins.
 */
public class JenkinsStatusThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(JenkinsStatusThread.class);

    private String ledDaemonUrl;
    private long jenkinsPollRate;
    private String[] jenkinsJobStatusUrls;

    public JenkinsStatusThread(String ledDaemonUrl, long jenkinsPollRate, String[] jenkinsJobStatusUrls)
    {
        this.ledDaemonUrl = ledDaemonUrl;
        this.jenkinsJobStatusUrls = jenkinsJobStatusUrls;
        this.jenkinsPollRate = jenkinsPollRate;
    }

    @Override
    public void run()
    {
        while (!isExit())
        {
            try
            {
                // Poll Jenkins
                LedPattern ledPattern = pollJenkins();

                // Update build indicator LED strip
                changePattern(ledPattern);

                // Wait a while...
                Thread.sleep(jenkinsPollRate);
            }
            catch (InterruptedException e)
            {
                LOG.error("Exception during Jenkins status thread", e);
            }
        }
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
            String response = Streams.readInputStream(httpResponse.getEntity().getContent(), 8192);

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonRoot = (JSONObject) jsonParser.parse(response);

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

    private void changePattern(LedPattern pattern)
    {
        try
        {
            // Build request body
            JSONObject jsonRoot = new JSONObject();
            jsonRoot.put("pattern", pattern.PATTERN);

            String json = jsonRoot.toJSONString();

            // Make request
            HttpClient httpClient = HttpClients.createMinimal();

            HttpPost httpPost = new HttpPost(ledDaemonUrl);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(json));

            httpClient.execute(httpPost);

            LOG.debug("LED daemon request sent - pattern: {}", pattern.PATTERN);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to LED daemon - url: {}, pattern: {}", ledDaemonUrl, pattern.PATTERN);
        }
        catch (Exception e)
        {
            LOG.error("Failed to change LED pattern", e);
        }
    }

}
