package com.limpygnome.daemon.interval.led.pattern.source;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Notification;
import com.limpygnome.daemon.api.ScreenAction;
import com.limpygnome.daemon.api.LedPattern;
import com.limpygnome.daemon.buildtv.model.Notification;
import com.limpygnome.daemon.buildtv.service.HardwareCommsService;
import com.limpygnome.daemon.buildtv.service.NotificationService;
import com.limpygnome.daemon.service.ClientCommsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Represents a pattern to be used during a specific interval of time of day.
 */
public class IntervalPatternSource extends PatternSource
{
    private static final Logger LOG = LogManager.getLogger(IntervalPatternSource.class);

    private static final String NOTIFICATION_SOURCE = "pattern-source";

    /**
     * The default priority for notifications created by this type.
     */
    public static final int NOTIFICATION_DEFAULT_PRIORITY = 25;

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
    public void eventNoLongerCurrentPatternSource(Controller controller)
    {
        removeNotification(controller);
    }

    @Override
    public void update(Controller controller, ClientCommsService hardwareCommsService)
    {
        // Turn screen on/off
        if (screenOff)
        {
            hardwareCommsService.changeScreen(controller, ScreenAction.OFF);
        }
        else
        {
            hardwareCommsService.changeScreen(controller, ScreenAction.ON);
        }
    }

    private void sendOutNotification(Controller controller)
    {
        if (notification != null)
        {
            LOG.debug("Updating notification... - name: {}, notification: {}", getName(), notification);

            // Fetch notification service
            NotificationService notificationService = (NotificationService) controller.getServiceByName(
                    NotificationService.SERVICE_NAME
            );

            // Set current notification to our notification
            notificationService.updateCurrentNotification(NOTIFICATION_SOURCE, notification);
        }
    }

    private void removeNotification(Controller controller)
    {
        if (notification != null)
        {
            LOG.debug("Removing notification... - name: {}, notification: {}", getName(), notification);

            // Fetch notification service
            NotificationService notificationService = (NotificationService) controller.getServiceByName(
                    NotificationService.SERVICE_NAME
            );

            // Remove source
            notificationService.removeNotificationSource(NOTIFICATION_SOURCE);
        }
    }

    @Override
    public String toString()
    {
        return  super.toString() + "->[" +
                "start: " + startMinuteOfDay + ", " +
                "end: " + endMinuteOfDay + ", " +
                "notification: " + notification +
                "]";
    }

}
