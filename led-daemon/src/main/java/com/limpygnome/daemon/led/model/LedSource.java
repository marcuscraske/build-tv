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
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LedSource ledSource = (LedSource) o;

        if (priority != ledSource.priority) return false;
        if (source != null ? !source.equals(ledSource.source) : ledSource.source != null) return false;
        return !(pattern != null ? !pattern.equals(ledSource.pattern) : ledSource.pattern != null);
    }

    @Override
    public int hashCode()
    {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        result = 31 * result + (int) (priority ^ (priority >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return "[source: " + source + ", pattern: " + pattern.getName() + ", priority: " + priority + "]";
    }

}
