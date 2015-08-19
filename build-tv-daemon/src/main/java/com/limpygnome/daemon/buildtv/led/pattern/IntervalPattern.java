package com.limpygnome.daemon.buildtv.led.pattern;

import com.limpygnome.daemon.buildtv.led.LedDisplayPatterns;
import org.joda.time.DateTime;

/**
 * Represents a pattern to be used during a specific interval of time of day.
 */
public class IntervalPattern extends Pattern
{
    private int startMinuteOfDay;
    private int endMinuteOfDay;
    private boolean screenOff;

    public IntervalPattern(String name, LedDisplayPatterns initialCurrentLedPattern, int priority,
                           int startHour, int startMinute, int endHour, int endMinute,
                           boolean screenOff)
    {
        super(name, initialCurrentLedPattern, priority);

        // Convert time to minute of day - more efficient and easier to deal with
        startMinuteOfDay = (startHour * 60) + startMinute;
        endMinuteOfDay = (endHour * 60) + endMinute;

        this.screenOff = screenOff;
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

    public boolean isScreenOff()
    {
        return screenOff;
    }
}
