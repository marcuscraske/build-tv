package com.limpygnome.daemon.buildtv.led.pattern.source;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.buildtv.led.ScreenAction;
import com.limpygnome.daemon.buildtv.led.pattern.LedPattern;
import com.limpygnome.daemon.buildtv.model.Notification;
import com.limpygnome.daemon.buildtv.service.NotificationService;
import com.limpygnome.daemon.util.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

import java.net.ConnectException;

/**
 * Represents a pattern to be used during a specific interval of time of day.
 */
public class IntervalPatternSource extends PatternSource
{
    private static final Logger LOG = LogManager.getLogger(IntervalPatternSource.class);

    private int startMinuteOfDay;
    private int endMinuteOfDay;
    private boolean screenOff;
    private Notification notification;

    public IntervalPatternSource(String name, LedPattern initialCurrentLedPattern, int priority,
                                 int startHour, int startMinute, int endHour, int endMinute,
                                 boolean screenOff, Notification notification)
    {
        super(name, initialCurrentLedPattern, priority);

        // Convert time to minute of day - more efficient and easier to deal with
        startMinuteOfDay = (startHour * 60) + startMinute;
        endMinuteOfDay = (endHour * 60) + endMinute;

        this.screenOff = screenOff;
        this.notification = notification;
    }

    @Override
    public boolean isEnabled()
    {
        DateTime dateTimeNow = DateTime.now();
        int minuteOfDay = dateTimeNow.getMinuteOfDay();

        // Exclusive time period of day
        if (endMinuteOfDay < startMinuteOfDay)
        {
            return minuteOfDay < endMinuteOfDay || minuteOfDay >= startMinuteOfDay;
        }
        // Normal inclusive time between two points during day
        else
        {
            return minuteOfDay >= startMinuteOfDay && minuteOfDay < endMinuteOfDay;
        }
    }

    @Override
    public void eventNowCurrentPatternSource(Controller controller)
    {
        // Send out notification
        sendOutNotification(controller);
    }

    @Override
    public void update(Controller controller)
    {
        // Turn screen on/off
        if (screenOff)
        {
            changeScreen(controller, ScreenAction.OFF);
        }
        else
        {
            changeScreen(controller, ScreenAction.ON);
        }
    }

    private void sendOutNotification(Controller controller)
    {
        if (notification != null)
        {
            // Fetch notification service
            NotificationService notificationService = (NotificationService) controller.getServiceByName(
                    NotificationService.SERVICE_NAME
            );

            // Set current notification to our notification
            notificationService.updateCurrentNotification(notification);
        }
    }

    private void changeScreen(Controller controller, ScreenAction screenAction)
    {
        // Fetch system-daemon screen endpoint
        String systemDaemonScreenEndpoint = controller.getSettings().getString("system-daemon.screen.rest.url");

        try
        {
            // Build JSON object
            JSONObject jsonRoot = new JSONObject();
            jsonRoot.put("action", screenAction.ACTION);

            // Make request
            RestClient restClient = new RestClient();
            restClient.executePost(systemDaemonScreenEndpoint, jsonRoot);

            LOG.debug("Screen action sent - action: {}", screenAction);
        }
        catch (ConnectException e)
        {
            LOG.error("Failed to connect to system daemon - url: {}", systemDaemonScreenEndpoint);
        }
        catch (Exception e)
        {
            LOG.error("Failed to make system daemon request", e);
        }
    }

}
