package com.limpygnome.daemon.led.model;

import com.limpygnome.daemon.led.hardware.pattern.Pattern;

/**
 * Represents a LED pattern from a source.
 */
public class LedSource
{
    private String source;
    private Pattern pattern;
    private long priority;

    public LedSource(String source, Pattern pattern, long priority)
    {
        this.source = source;
        this.pattern = pattern;
        this.priority = priority;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public void setPattern(Pattern pattern)
    {
        this.pattern = pattern;
    }

    public long getPriority()
    {
        return priority;
    }

    public void setPriority(long priority)
    {
        this.priority = priority;
    }

    @Override
    public String toString()
    {
        return "[source: " + source + ", pattern: " + pattern.getName() + ", priority: " + priority + "]";
    }

}
