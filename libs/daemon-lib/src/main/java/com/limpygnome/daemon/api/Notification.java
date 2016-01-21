package com.limpygnome.daemon.api;

/**
 * Represents a notification, intended to be displayed on the screen/VHD connected to the system.
 */
public class Notification
{
    private String header;
    private String text;
    private long lifespan;
    private String type;
    private int priority;

    public Notification(String header, String text, long lifespanMs, String type, int priority)
    {
        this.header = header;
        this.text = text;
        this.lifespan = lifespanMs;
        this.type = type;
        this.priority = priority;
    }

    public String getHeader()
    {
        return header;
    }

    public String getText()
    {
        return text;
    }

    public long getLifespan()
    {
        return lifespan;
    }

    public String getType()
    {
        return type;
    }

    public int getPriority()
    {
        return priority;
    }

    @Override
    public String toString()
    {
        return  "[" +
                "header: " + header + ", " +
                "text: " + text + ", " +
                "lifespan: " + lifespan + ", " +
                "priority: " + priority +
                "type: " + type +
                "]";
    }
}
