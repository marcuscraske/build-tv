package com.limpygnome.daemon.buildtv.model;

import java.awt.*;

/**
 * Created by limpygnome on 27/08/15.
 */
public class Notification
{
    private String header;
    private String text;
    private long lifespan;
    private Color background;

    private long timeStamp;

    public Notification(String header, String text, long lifespan, Color background)
    {
        this.header = header;
        this.text = text;
        this.lifespan = lifespan;
        this.background = background;
        this.timeStamp = System.currentTimeMillis();
    }

    public boolean isDifferentBesidesTimestamp(Notification notification)
    {
        return  (header != null && !header.equals(notification.header)) ||
                (text != null && !text.equals(notification.text)) ||
                lifespan != notification.lifespan ||
                (background != null && !background.equals(notification.background));
    }

    public String getHeader()
    {
        return header;
    }

    public void setHeader(String header)
    {
        this.header = header;
    }

    public String getText()
    {
        return text;
    }

    /**
     * Sets the text displayed with the notification.
     *
     * @param text The text; can be null
     */
    public void setText(String text)
    {
        this.text = text;
    }

    public long getLifespan()
    {
        return lifespan;
    }

    public void setLifespan(long lifespan)
    {
        this.lifespan = lifespan;
    }

    public Color getBackground()
    {
        return background;
    }

    public void setBackground(Color background)
    {
        this.background = background;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }
}
