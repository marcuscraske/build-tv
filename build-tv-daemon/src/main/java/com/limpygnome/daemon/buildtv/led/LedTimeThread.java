package com.limpygnome.daemon.buildtv.led;

import com.limpygnome.daemon.buildtv.led.pattern.source.IntervalPatternSource;
import com.limpygnome.daemon.buildtv.led.pattern.LedPatterns;
import com.limpygnome.daemon.buildtv.led.pattern.source.PatternSource;
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

    private static final String LED_DAEMON_SOURCE = "build-tv";

    private String ledDaemonUrl;
    private long ledDaemonPriority;
    private String screenDaemonUrl;
    private HashMap<String, PatternSource> patterns;

    public LedTimeThread(String ledDaemonUrl, long ledDaemonPriority, String screenDaemonUrl)
    {
        this.ledDaemonUrl = ledDaemonUrl;
        this.ledDaemonPriority = ledDaemonPriority;
        this.screenDaemonUrl = screenDaemonUrl;
        this.patterns = new HashMap<>();
    }

    /**
     * Adds a source for an LED pattern.
     *
     * @param patternSource The pattern to add
     */
    public synchronized void addPattern(PatternSource patternSource)
    {
        patterns.put(patternSource.getName(), patternSource);
        LOG.debug("Added pattern source - name: {}, priority: {}", patternSource.getName(), patternSource.getPriority());
    }

    public synchronized void removePattern(PatternSource patternSource)
    {
        patterns.remove(patternSource.getName());
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
            currentPatternSource = findHighestPriorityPattern();

            // Update pattern by talking to LED daemon
            if (currentPatternSource != null)
            {
                changePattern(currentPatternSource.getCurrentLedPattern());
            }
            else
            {
                changePattern(LedPatterns.BUILD_UNKNOWN);
            }

            // Update screen on/off
            if  (   currentPatternSource != null &&
                    currentPatternSource instanceof IntervalPatternSource &&
                    ((IntervalPatternSource) currentPatternSource).isScreenOff()
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

    private synchronized PatternSource findHighestPriorityPattern()
    {
        // Iterate patterns and retrieve enabled source with highest priority
        PatternSource patternSource;
        PatternSource highestPatternSource = null;
        int highestPriority = -1;

        for (Map.Entry<String, PatternSource> patternSourceKV : patterns.entrySet())
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

    private void changePattern(LedPatterns pattern)
    {
        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();
            jsonRoot.put("source", LED_DAEMON_SOURCE);
            jsonRoot.put("pattern", pattern.PATTERN);
            jsonRoot.put("priority", ledDaemonPriority);

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
