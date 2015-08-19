package com.limpygnome.daemon.buildtv.led;

import com.limpygnome.daemon.buildtv.led.pattern.IntervalPattern;
import com.limpygnome.daemon.buildtv.led.pattern.Pattern;
import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to decide which pattern to render.
 */
public class LedTimeThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(LedTimeThread.class);

    private String ledDaemonUrl;
    private String screenDaemonUrl;
    private HashMap<String, Pattern> patterns;

    public LedTimeThread(String ledDaemonUrl, String screenDaemonUrl)
    {
        this.ledDaemonUrl = ledDaemonUrl;
        this.screenDaemonUrl = screenDaemonUrl;
        this.patterns = new HashMap<>();
    }

    /**
     * Adds a source for an LED pattern.
     *
     * @param pattern The pattern to add
     */
    public synchronized void addPattern(Pattern pattern)
    {
        patterns.put(pattern.getName(), pattern);
        LOG.debug("Added pattern source - name: {}, priority: {}", pattern.getName(), pattern.getPriority());
    }

    public synchronized void removePattern(Pattern pattern)
    {
        patterns.remove(pattern.getName());
        LOG.debug("Removed pattern source - name: {}", pattern.getName());
    }

    @Override
    public void run()
    {
        final long UPDATE_INTERVAL = 1000;

        Pattern currentPattern;

        while (!isExit())
        {
            // Fetch the current pattern
            currentPattern = findHighestPriorityPattern();

            // Update pattern by talking to LED daemon
            if (currentPattern != null)
            {
                changePattern(currentPattern.getCurrentLedPattern());
            }
            else
            {
                changePattern(LedDisplayPatterns.BUILD_UNKNOWN);
            }

            // Update screen on/off
            if  (   currentPattern != null &&
                    currentPattern instanceof IntervalPattern &&
                    ((IntervalPattern) currentPattern).isScreenOff()
                )
            {
                changeScreen(ScreenAction.OFF);
            }
            else
            {
                changeScreen(ScreenAction.ON);
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

    private synchronized Pattern findHighestPriorityPattern()
    {
        // Iterate patterns and retrieve enabled source with highest priority
        Pattern pattern;
        Pattern highestPattern = null;
        int highestPriority = -1;

        for (Map.Entry<String, Pattern> patternSourceKV : patterns.entrySet())
        {
            pattern = patternSourceKV.getValue();

            if (pattern.getPriority() > highestPriority && pattern.isEnabled())
            {
                highestPattern = pattern;
                highestPriority = pattern.getPriority();
            }
        }

        return highestPattern;
    }

    private void changePattern(LedDisplayPatterns pattern)
    {
        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();
            jsonRoot.put("pattern", pattern.PATTERN);

            // Make request
            RestClient restClient = new RestClient();
            restClient.executePost(ledDaemonUrl, jsonRoot);

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

    private void changeScreen(ScreenAction screenAction)
    {
        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();
            jsonRoot.put("action", screenAction.ACTION);

            // Make request
            RestClient restClient = new RestClient();
            restClient.executePost(screenDaemonUrl, jsonRoot);

            LOG.debug("Screen action sent - action: {}", screenAction);
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
