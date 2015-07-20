package com.limpygnome.daemon.buildtv.led;

import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.util.RestUtil;
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
    private String screenDaemonUrl;
    private HashMap<String, PatternSource> patternSources;

    public LedTimeThread(String ledDaemonUrl, String screenDaemonUrl)
    {
        this.ledDaemonUrl = ledDaemonUrl;
        this.screenDaemonUrl = screenDaemonUrl;
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

        PatternSource currentPatternSource;

        while (!isExit())
        {
            // Fetch the current pattern
            currentPatternSource = findHighestPriorityPatternSource();

            // Update pattern by talking to LED daemon
            if (currentPatternSource != null)
            {
                changePattern(currentPatternSource.getCurrentLedPattern());
            }
            else
            {
                changePattern(LedPattern.BUILD_UNKNOWN);
            }

            // Update screen on/off
            if  (   currentPatternSource != null &&
                    currentPatternSource instanceof IntervalPattern &&
                    ((IntervalPattern) currentPatternSource).isScreenOff()
                )
            {
                changeScreen(false);
            }
            else
            {
                changeScreen(true);
            }

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

    private synchronized PatternSource findHighestPriorityPatternSource()
    {
        // Iterate patterns and retrieve enabled source with highest priority
        PatternSource patternSource;
        PatternSource highestPatternSource = null;
        int highestPriority = -1;

        for (Map.Entry<String, PatternSource> patternSourceKV : patternSources.entrySet())
        {
            patternSource = patternSourceKV.getValue();

            if (patternSource.getPriority() > highestPriority && patternSource.isEnabled())
            {
                highestPatternSource = patternSource;
                highestPriority = patternSource.getPriority();
            }
        }

        return highestPatternSource;
    }

    private void changePattern(LedPattern pattern)
    {
        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();
            jsonRoot.put("pattern", pattern.PATTERN);

            // Make request
            RestUtil.httpPostRequest(ledDaemonUrl, jsonRoot);

            LOG.debug("LED daemon update request sent - pattern: {}", pattern.PATTERN);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to LED daemon - url: {}, pattern: {}", ledDaemonUrl, pattern.PATTERN);
        }
        catch (Exception e)
        {
            LOG.error("Failed to make LED daemon request", e);
        }
    }

    private void changeScreen(boolean screenOn)
    {
        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();
            // TODO: turn into enum...
            jsonRoot.put("action", screenOn ? "on" : "off");

            // Make request
            RestUtil.httpPostRequest(screenDaemonUrl, jsonRoot);

            LOG.debug("Screen action sent - on: {}", screenOn);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to screen daemon - url: {}", ledDaemonUrl);
        }
        catch (Exception e)
        {
            LOG.error("Failed to make screen daemon request", e);
        }
    }

}
