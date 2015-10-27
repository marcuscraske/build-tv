package com.limpygnome.daemon.buildtv.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A container for a notification, with metadata regarding its priority and source.
 *
 * A source may contain only a single notification.
 */
public class NotificationSource implements Comparable<NotificationSource>
{
    private static final Logger LOG = LogManager.getLogger(NotificationSource.class);

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
        return (notification.getTimeStamp() + notification.getLifespan()) < System.currentTimeMillis();
    }

    public String getSource()
    {
        return source;
    }

    public void setNotification(Notification notification)
    {
        // Check new notification is different and has the same or greater priority
        if (this.notification.getPriority() > notification.getPriority())
        {
            LOG.debug("Notification not updated, priority is less than current notification from source");
        }
        else
        {
            this.notification = notification;
        }
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
