package com.limpygnome.daemon.buildtv.led;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ControllerState;
import com.limpygnome.daemon.buildtv.led.pattern.source.IntervalPatternSource;
import com.limpygnome.daemon.buildtv.led.pattern.LedPattern;
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

    private Controller controller;
    private String ledDaemonUrlLeds;
    private long ledDaemonPriority;
    private HashMap<String, PatternSource> patterns;

    public LedTimeThread(Controller controller, String ledDaemonUrl, long ledDaemonPriority)
    {
        this.controller = controller;
        this.ledDaemonUrlLeds = ledDaemonUrl;
        this.ledDaemonPriority = ledDaemonPriority;
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
        LOG.debug("Added pattern source - source: {}", patternSource);
    }

    public synchronized void removePattern(PatternSource patternSource)
    {
        patterns.remove(patternSource.getName());
        LOG.debug("Removed pattern source - name: {}", patternSource.getName());
    }

    @Override
    public void run()
    {
        // Wait for services to startup...
        controller.waitForState(ControllerState.RUNNING);

        // Update LED pattern based on highest source
        // TODO: could be improved with semaphores / state based
        final long UPDATE_INTERVAL = 1000;

        PatternSource currentPatternSource;
        IntervalPatternSource intervalPatternSource;

        PatternSource lastPatternSource = null;

        while (!isExit())
        {
            // Fetch the current pattern
            currentPatternSource = findHighestPriorityPattern();

            // Check if pattern has changed
            if (lastPatternSource == null || currentPatternSource != lastPatternSource)
            {
                // Inform current pattern they're now the current pattern
                if (currentPatternSource != null)
                {
                    currentPatternSource.eventNowCurrentPatternSource(controller);
                    LOG.debug("Current pattern changed - pattern: {}", currentPatternSource);
                }

                lastPatternSource = currentPatternSource;
            }

            // Update pattern by talking to LED daemon
            if (currentPatternSource != null)
            {
                // Update LED pattern
                changePattern(currentPatternSource.getCurrentLedPattern());

                // Run pattern logic
                currentPatternSource.update(controller);
            }
            else
            {
                changePattern(LedPattern.BUILD_UNKNOWN);
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

    private void changePattern(LedPattern pattern)
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
            restClient.executePost(ledDaemonUrlLeds, jsonRoot);

            LOG.debug("LED daemon update request sent - pattern: {}", pattern.PATTERN);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to LED daemon - url: {}, pattern: {}", ledDaemonUrlLeds, pattern.PATTERN);
        }
        catch (Exception e)
        {
            LOG.error("Failed to make LED daemon request", e);
        }
    }

}
