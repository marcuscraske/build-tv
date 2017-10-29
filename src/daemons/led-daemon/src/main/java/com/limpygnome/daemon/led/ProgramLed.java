package com.limpygnome.daemon.led;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.imp.DefaultController;
import com.limpygnome.daemon.led.hardware.pattern.daemon.Test;
import com.limpygnome.daemon.led.service.LedRestService;
import com.limpygnome.daemon.led.service.LedService;
import com.limpygnome.daemon.service.RestService;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * The entry point into the LED daemon.
 */
public class ProgramLed
{
    private static final Logger LOG = LogManager.getLogger(ProgramLed.class);

    private static final String CONTROLLER_NAME = "led-daemon";

    enum Mode
    {
        DAEMON,
        LED_TEST
    }


    public static void main(String[] args)
    {
        // Determine mode of application
        Mode mode = Mode.DAEMON;

        if (args != null && args.length > 0)
        {
            if (args[0] != null && args[0].equals("test"))
            {
                mode = Mode.LED_TEST;
            }
        }

        // Handle mode
        switch (mode)
        {
            case DAEMON:
                runDaemon();
                break;
            case LED_TEST:
                runLedTest();
                break;
        }
    }

    public static void runDaemon()
    {
        Controller controller = new DefaultController(CONTROLLER_NAME);

        // Add services
        controller.add(LedService.SERVICE_NAME, new LedService());
        controller.add(LedRestService.SERVICE_NAME, new LedRestService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

    public static void runLedTest()
    {
        // Create minimal controller to read settings
        Controller controller = new DefaultController(CONTROLLER_NAME);

        // Build JSON to invoke test pattern
        JSONObject jsonRoot = new JSONObject();
        jsonRoot.put("source", "led-test");
        jsonRoot.put("pattern", new Test().getName());
        jsonRoot.put("priority", 9999);

        // Make request
        try
        {
            RestClient restClient = new RestClient();
            restClient.executePost("http://localhost:" + controller.getSettings().getLong("rest/port"), jsonRoot);

            LOG.info("Invoked LED daemon endpoint successfully");
        }
        catch (Exception e)
        {
            LOG.error("Failed to invoke test pattern on local LED daemon REST endpoint, is it running?", e);
        }
    }

}
