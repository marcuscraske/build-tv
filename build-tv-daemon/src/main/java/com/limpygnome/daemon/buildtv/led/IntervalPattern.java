package com.limpygnome.daemon.buildtv.led;

import org.joda.time.DateTime;

/**
 * Created by limpygnome on 19/07/15.
 */
public class IntervalPattern extends PatternSource
{
    private int startMinuteOfDay;
    private int endMinuteOfDay;

    public IntervalPattern(String name, LedPattern initialCurrentLedPattern, int priority,
                           int startHour, int startMinute, int endHour, int endMinute)
    {
        super(name, initialCurrentLedPattern, priority);

        // Convert time to minute of day - more efficient and easier to deal with
        startMinuteOfDay = (startHour * 60) + startMinute;
        endMinuteOfDay = (endHour * 60) + endMinute;
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
}