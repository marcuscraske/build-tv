package com.limpygnome.daemon.buildtv.led;

import com.limpygnome.daemon.common.ExtendedThread;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by limpygnome on 19/07/15.
 */
public class LedTimeThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(LedTimeThread.class);

    private String ledDaemonUrl;
    private HashMap<String, PatternSource> patternSources;

    public LedTimeThread(String ledDaemonUrl)
    {
        this.ledDaemonUrl = ledDaemonUrl;
        this.patternSources = new HashMap<>();
    }

    /**
     * Adds a source for an LED pattern.
     *
     * @param patternSource The pattern to add
     */
    public synchronized void addPatternSource(PatternSource patternSource)
    {
        patternSources.put(patternSource.getName(), patternSource);
        LOG.debug("Added pattern source - name: {}, priority: {}", patternSource.getName(), patternSource.getPriority());
    }

    public synchronized void removePatternSource(PatternSource patternSource)
    {
        patternSources.remove(patternSource.getName());
        LOG.debug("Removed pattern source - name: {}", patternSource.getName());
    }

    @Override
    public void run()
    {
        final long UPDATE_INTERVAL = 1000;

        LedPattern currentPattern;
        while (!isExit())
        {
            // Fetch the current pattern
            currentPattern = findCurrentPattern();

            // Update pattern by talking to LED daemon
            changePattern(currentPattern);

            // Sleep for a while
            try
            {
                Thread.sleep(UPDATE_INTERVAL);
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    private synchronized LedPattern findCurrentPattern()
    {
        // Iterate patterns and retrieve enabled source with highest priority
        PatternSource patternSource;
        LedPattern highestPattern = LedPattern.BUILD_UNKNOWN;
        int highestPriority = -1;

        for (Map.Entry<String, PatternSource> patternSourceKV : patternSources.entrySet())
        {
            patternSource = patternSourceKV.getValue();

            if (patternSource.getPriority() > highestPriority && patternSource.isEnabled())
            {
                highestPattern = patternSource.getCurrentLedPattern();
                highestPriority = patternSource.getPriority();
            }
        }

        return highestPattern;
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

            LOG.debug("LED daemon update request sent - pattern: {}", pattern.PATTERN);
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
