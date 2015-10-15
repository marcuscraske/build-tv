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

    @Override
    public synchronized void start(Controller controller)
    {
        String ledDaemonUrl = controller.getSettings().getString("led-daemon.rest.url");
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
