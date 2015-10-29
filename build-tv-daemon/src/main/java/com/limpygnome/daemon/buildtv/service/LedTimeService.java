package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.buildtv.led.LedTimeThread;
import com.limpygnome.daemon.buildtv.led.pattern.source.PatternSource;

/**
 * Responsible for controlling the LED requests sent to the LED daemon.
 */
public class LedTimeService implements Service
{
    private LedTimeThread ledTimeThread;

    public static final String SERVICE_NAME = "led-time";

    @Override
    public synchronized void start(Controller controller)
    {
        String ledDaemonUrl;

        // Check daemon is available
        if (controller.isDaemonEnabled("led-daemon"))
        {
            ledDaemonUrl = controller.getSettings().getString("led-daemon.rest.url");
        }
        else
        {
            ledDaemonUrl = null;
        }

        long ledDaemonPriority = controller.getSettings().getLong("led-daemon.priority");

        ledTimeThread = new LedTimeThread(
                controller,
                ledDaemonUrl,
                ledDaemonPriority
        );
        ledTimeThread.start();
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        ledTimeThread.kill();
    }

    public synchronized void addPatternSource(PatternSource patternSource)
    {
        ledTimeThread.addPattern(patternSource);
    }

    public synchronized void removePatternSource(PatternSource patternSource)
    {
        ledTimeThread.removePattern(patternSource);
    }
}
