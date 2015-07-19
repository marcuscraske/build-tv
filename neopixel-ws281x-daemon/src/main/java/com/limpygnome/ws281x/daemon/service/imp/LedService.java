package com.limpygnome.ws281x.daemon.service.imp;

import com.limpygnome.ws281x.daemon.Controller;
import com.limpygnome.ws281x.daemon.led.api.Pattern;
import com.limpygnome.ws281x.daemon.led.imp.LedController;
import com.limpygnome.ws281x.daemon.led.imp.LedRenderThread;
import com.limpygnome.ws281x.daemon.led.imp.patterns.build.*;
import com.limpygnome.ws281x.daemon.led.imp.patterns.daemon.Startup;
import com.limpygnome.ws281x.daemon.led.imp.patterns.daemon.*;
import com.limpygnome.ws281x.daemon.led.imp.patterns.team.Standup;
import com.limpygnome.ws281x.daemon.service.api.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/**
 * A service to control a NeoPixel LED strip.
 */
public class LedService implements Service
{
    private static final Logger LOG = LogManager.getLogger(LedService.class);

    private HashMap<String, Pattern> patterns;
    private LedController ledController;
    private LedRenderThread ledRenderThread;

    public LedService()
    {
        this.patterns = new HashMap<>();
        this.ledController = new LedController();
        this.ledRenderThread = null;
    }

    @Override
    public synchronized void start(Controller controller)
    {
        // Register all the patterns!
        patterns.put("build-ok", new BuildOkay());
        patterns.put("build-progress", new BuildProgress());
        patterns.put("build-unstable", new BuildUnstable());
        patterns.put("build-failure", new BuildFailure());
        patterns.put("jenkins-unavailable", new JenkinsUnavailable());

        patterns.put("shutdown", new Shutdown());
        patterns.put("startup", new Startup());

        patterns.put("standup", new Standup());


        // Set startup pattern, whilst another service changes it
        setPattern("startup");
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        // Change pattern to shutdown
        setPattern("shutdown");

        // Stop render thread
        ledRenderThread.kill();

        // Wipe patterns
        patterns.clear();
    }

    public synchronized void setPattern(String patternName)
    {
        // Locate pattern
        Pattern pattern = patterns.get(patternName);

        if (pattern == null)
        {
            LOG.warn("LED pattern missing - pattern: {}", patternName);
        }
        else
        {
            // Kill current thread
            if (ledRenderThread != null)
            {
                ledRenderThread.kill();
            }

            // Build new thread
            ledRenderThread = new LedRenderThread(this, pattern);
            ledRenderThread.start();

            LOG.debug("LED pattern changed - pattern: {}", patternName);
        }
    }

    public synchronized LedController getLedController()
    {
        return ledController;
    }

}
