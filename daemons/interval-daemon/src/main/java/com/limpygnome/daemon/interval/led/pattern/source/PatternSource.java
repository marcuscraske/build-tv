package com.limpygnome.daemon.interval.led.pattern.source;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.ScreenAction;
import com.limpygnome.daemon.api.LedPattern;
import com.limpygnome.daemon.interval.led.ClientAggregate;

/**
 * Represents a source of a LED pattern, with the priority and enable status used to determine which
 * source's LED pattern should be used.
 */
public class PatternSource
{
    private String name;
    private LedPattern currentLedPattern;
    private int priority;

    public PatternSource(String name, LedPattern initialCurrentLedPattern, int priority)
    {
        this.name = name;
        this.currentLedPattern = initialCurrentLedPattern;
        this.priority = priority;
    }

    /**
     * Indicates if the pattern is currently enabled. Inheriting classes should override this method for logically
     * deciding if they're active/enabled.
     *
     * @return True = enabled / active, false = disabled
     */
    public boolean isEnabled()
    {
        return true;
    }

    public String getName()
    {
        return name;
    }

    public LedPattern getCurrentLedPattern()
    {
        return currentLedPattern;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setCurrentLedPattern(LedPattern currentLedPattern)
    {
        this.currentLedPattern = currentLedPattern;
    }

    /**
     * Invoked once when this pattern becomes the current pattern source.
     *
     * @param controller The current instance of the controller
     * @param clientAggregate Available clients
     */
    public void eventNowCurrentPatternSource(Controller controller, ClientAggregate clientAggregate)
    {
        // Nothing by default...
    }

    /**
     * Invoked when this pattern is no longer the current pattern, just before the next pattern has its
     * {@link #eventNowCurrentPatternSource(Controller, ClientAggregate)} method invoked.
     *
     * @param controller The current instance of the controller
     * @param clientAggregate Available clients
     */
    public void eventNoLongerCurrentPatternSource(Controller controller, ClientAggregate clientAggregate)
    {
        // Nothing by default...
    }

    /**
     * Invoked every time the current source pattern is updated and is this instance.
     *
     * @param controller The current instance of the controller
     * @param clientAggregate Available clients
     */
    public void update(Controller controller, ClientAggregate clientAggregate)
    {
        // By default, keep the screen on...
        clientAggregate.getScreenClient().changeScreen(controller, ScreenAction.ON);
    }

    @Override
    public String toString()
    {
        return  "[" +
                "name: " + name + ", " +
                "pattern: " + currentLedPattern.PATTERN + ", " +
                "priority: " + priority +
                "]";
    }

}
