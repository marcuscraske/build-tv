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
    private static final long ACTION_TIMEOUT_MS = 10000;

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
        initialState();
    }

    private void initialState()
    {
        this.lastAction = 0;
        this.screenOn = false;
    }

    @Override
    public synchronized void start(Controller controller)
    {
        // Make sure the screen is initially on
        screenOn();
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        initialState();
    }

    public synchronized void screenOn()
    {
        if (!isTooSoon() && !this.screenOn)
        {
            LOG.info("Turning screen on...");

            exec("/opt/vc/bin/tvservice -p");
            exec("fbset -depth 16");
            exec("fbset -depth 0");
            exec("fbset -depth 16");
            exec("fbset -accel true");

            this.screenOn = true;
        }
    }

    public synchronized void screenOff()
    {
        if (!isTooSoon() && this.screenOn)
        {
            LOG.info("Turning screen off...");

            exec("/opt/vc/bin/tvservice -o");

            this.screenOn = false;
        }
    }

    private boolean isTooSoon()
    {
        long currTime = System.currentTimeMillis();

        if (currTime - lastAction > ACTION_TIMEOUT_MS)
        {
            lastAction = currTime;
            return false;
        }

        LOG.debug("Cannot perform screen operation, timeout in effect...");
        return true;
    }

    private void exec(String command)
    {
        // Execute the command
        EnvironmentUtil.exec(command, PROCESS_TIMEOUT);

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

    public boolean handleScreenSet(RestRequest restRequest, RestResponse restResponse)
    {
        // Check request has JSON body
        String action = (String) restRequest.getJsonElement(new String[]{ "action" });

        if (action != null && action.length() > 0)
        {
            switch (action)
            {
                case "on":
                    screenOn();
                    return true;
                case "off":
                    screenOff();
                    return true;
            }
        }

        return false;
    }

    public boolean handleScreenGet(RestRequest restRequest, RestResponse restResponse)
    {
        // Build response
        JSONObject response = new JSONObject();
        response.put("on", screenOn);

        // Write response
        restResponse.writeJsonResponseIgnoreExceptions(restResponse, response);

        return true;
    }

}
