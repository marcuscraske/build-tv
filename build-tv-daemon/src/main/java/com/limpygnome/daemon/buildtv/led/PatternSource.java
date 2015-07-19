package com.limpygnome.daemon.buildtv.led;

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
}
