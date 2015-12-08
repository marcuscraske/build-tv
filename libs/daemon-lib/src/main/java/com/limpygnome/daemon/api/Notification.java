package com.limpygnome.daemon.api;

import java.awt.*;

/**
 * Represents a notification, intended to be displayed on the screen/VHD connected to the system.
 */
public class Notification
{
    private String header;
    private String text;
    private long lifespan;
    private Color background;
    private int priority;

    public Notification(String header, String text, long lifespanMs, Color background, int priority)
    {
        this.header = header;
        this.text = text;
        this.lifespan = lifespanMs;
        this.background = background;
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

    public Color getBackground()
    {
        return background;
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
                "]";
    }
}
