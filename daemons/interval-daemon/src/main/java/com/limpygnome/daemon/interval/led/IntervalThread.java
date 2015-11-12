package com.limpygnome.daemon.interval.led;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ControllerState;
import com.limpygnome.daemon.interval.led.pattern.source.IntervalPatternSource;
import com.limpygnome.daemon.api.LedPattern;
import com.limpygnome.daemon.interval.led.pattern.source.PatternSource;
import com.limpygnome.daemon.common.ExtendedThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to decide which pattern to render.
 */
public class IntervalThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(IntervalThread.class);

    private Controller controller;
    private ClientAggregate clientAggregate;
    private HashMap<String, PatternSource> patterns;

    /**
     * Creates a new instance.
     *
     * @param controller The current controller
     */
    public IntervalThread(Controller controller)
    {
        this.controller = controller;
        this.clientAggregate = new ClientAggregate(controller);
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
                // Inform current pattern source they're no longer the current pattern
                if (lastPatternSource != null)
                {
                    lastPatternSource.eventNoLongerCurrentPatternSource(controller, clientAggregate);
                }

                // Inform current pattern they're now the current pattern
                if (currentPatternSource != null)
                {
                    currentPatternSource.eventNowCurrentPatternSource(controller, clientAggregate);
                }

                LOG.debug("Current pattern changed - old pattern: {}, new pattern: {}", lastPatternSource, currentPatternSource);
                lastPatternSource = currentPatternSource;
            }

            // Update pattern by talking to LED daemon
            if (currentPatternSource != null)
            {
                // Update LED pattern
                clientAggregate.getLedClient().changeLedPattern(currentPatternSource.getCurrentLedPattern());

                // Run pattern logic
                currentPatternSource.update(controller, clientAggregate);
            }
            else
            {
                clientAggregate.getLedClient().changeLedPattern(LedPattern.BUILD_UNKNOWN);
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

}
