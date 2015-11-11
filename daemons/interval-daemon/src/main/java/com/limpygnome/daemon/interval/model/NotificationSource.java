package com.limpygnome.daemon.interval.model;

import com.limpygnome.daemon.api.Notification;

/**
 * A container for a notification, with metadata regarding its priority and source.
 *
 * A source may contain only a single notification.
 */
public class NotificationSource implements Comparable<NotificationSource>
{
    private String source;
    private Notification notification;

    public NotificationSource(String source, Notification notification)
    {
        this.source = source;
        this.notification = notification;
    }

    /**
     * Checks if the current notification has expired.
     *
     * @return True = expired, false = not expired
     */
    public boolean isExpired()
    {
        if (notification == null)
        {
            // No longer need source, no notification present...
            return true;
        }

        long lifeSpan = notification.getLifespan();

        if (lifeSpan > 0)
        {
            long currentTime = System.currentTimeMillis();
            long expireTime = notification.getTimeStamp() + lifeSpan;

            return currentTime > expireTime;
        }
        else
        {
            return false;
        }
    }

    public String getSource()
    {
        return source;
    }

    public void setNotification(Notification notification)
    {
        this.notification = notification;
    }

    public Notification getNotification()
    {
        return notification;
    }

    @Override
    public int compareTo(NotificationSource notificationSource)
    {
        return notification.getPriority() - notificationSource.getNotification().getPriority();
    }

}
