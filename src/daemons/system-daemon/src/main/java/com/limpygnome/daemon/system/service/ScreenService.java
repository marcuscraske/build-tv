package com.limpygnome.daemon.system.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.common.rest.RestRequest;
import com.limpygnome.daemon.common.rest.RestResponse;
import com.limpygnome.daemon.api.RestServiceHandler;
import com.limpygnome.daemon.util.EnvironmentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * A service used to control the attached screen.
 */
public class ScreenService implements Service, RestServiceHandler
{
    private static final Logger LOG = LogManager.getLogger(ScreenService.class);

    public static final String SERVICE_NAME = "screen";

    /**
     * The timeout between requests. Any requests during the timeout period are ignored. It's expected that
     * external services will periodically call this daemon, irregardless of the screen state changing. THis is to
     * allow the daemon to restart or for the external screen to get out of sync.
     */
    private static final long ACTION_TIMEOUT_MS = 30000;

    /**
     * Timeout in executing a process before forcibly killing it.
     */
    private static final long PROCESS_TIMEOUT = 5000;

    /**
     * Delay between executing screen commands.
     */
    private static final long COMMAND_DELAY = 4000;


    private long lastAction;
    private boolean screenOn;

    public ScreenService()
    {
    }

    private void initialState()
    {
        this.lastAction = 0;
        this.screenOn = true;
    }

    @Override
    public synchronized void start(Controller controller)
    {
        // Make sure the screen is initially on
        screenOn(true);

        // Set initial state
        initialState();
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        initialState();
    }

    public synchronized void screenOn(boolean force)
    {
        if (force || (!isTooSoon() && isScreenExpectedState(false)))
        {
            LOG.info("Executing command(s) for screen on...");

            // Causes framebuffer corruption in latest Pi's when using fbset sometimes, tvservice also has blank screen
            // when turning back on. Disabled for now, left as future alternative...
//            exec(new String[]{ "/opt/vc/bin/tvservice", "-p" });
//            exec(new String[]{ "fbset", "-depth", "8" });
//            exec(new String[]{ "fbset", "-depth", "16" });

            exec(new String[]{ "/bin/sh", "-c", "echo \"on 0\" | cec-client -s -d 1" });

            this.screenOn = true;
        }
    }

    public synchronized void screenOff(boolean force)
    {
        if (force || (!isTooSoon() && isScreenExpectedState(true)))
        {
            LOG.info("Executing command(s) for screen off...");

            // See comments above for screenOn; using cec-client as alternative
//            exec(new String[]{ "/opt/vc/bin/tvservice", "-o" });

            exec(new String[]{ "/bin/sh", "-c", "echo \"standby 0\" | cec-client -s -d 1" });

            this.screenOn = false;
        }
    }

    private boolean isTooSoon()
    {
        long currTime = System.currentTimeMillis();

        if (currTime - lastAction > ACTION_TIMEOUT_MS)
        {
            // Update last action to current time
            lastAction = currTime;
            return false;
        }

        LOG.debug("Cannot perform screen operation, timeout in effect - last action: {}, current time: {}",
                lastAction, currTime
        );

        return true;
    }

    private boolean isScreenExpectedState(boolean screenOnExpected)
    {
        if (screenOn != screenOnExpected)
        {
            if (screenOn)
            {
                LOG.debug("Screen already on, command ignored...");
            }
            else
            {
                LOG.debug("Screen already off, command ignored...");
            }

            return false;
        }

        return true;
    }

    private void exec(String[] command)
    {
        // Execute the command
        EnvironmentUtil.exec(command, PROCESS_TIMEOUT, true);

        // Give enough wait for the command to take affect
        // -- Hacky, but a new attempt at getting this to work through a daemon
        try
        {
            Thread.sleep(COMMAND_DELAY);
        }
        catch (InterruptedException e) { }
    }

    @Override
    public boolean handleRequestInChain(RestRequest restRequest, RestResponse restResponse)
    {
        // Check we can handle the request
        if (restRequest.isJsonRequest() && restRequest.isPathMatch(new String[]{"system-daemon", "screen", "set" }))
        {
            return handleScreenSet(restRequest, restResponse);
        }
        else if (restRequest.isPathMatch(new String[]{ "system-daemon", "screen", "get" }))
        {
            return handleScreenGet(restRequest, restResponse);
        }

        return false;
    }

    private boolean handleScreenSet(RestRequest restRequest, RestResponse restResponse)
    {
        // Check request has JSON body
        String action = (String) restRequest.getJsonElement(new String[]{ "action" });

        if (action != null && action.length() > 0)
        {
            switch (action)
            {
                case "on":
                    screenOn(false);
                    return true;
                case "off":
                    screenOff(false);
                    return true;
            }
        }

        return false;
    }

    private boolean handleScreenGet(RestRequest restRequest, RestResponse restResponse)
    {
        // Build response
        JSONObject response = new JSONObject();
        response.put("on", screenOn);

        // Write response
        restResponse.writeJsonResponseIgnoreExceptions(response);

        return true;
    }

}
