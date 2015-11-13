package com.limpygnome.client.notification.ui.effect;

import com.limpygnome.client.notification.ui.NotificationWindow;
import com.limpygnome.daemon.common.ExtendedThread;

/**
 * A UI effect to gradually increase the opacity of the notification window.
 */
public class NotificationWindowFlashThread extends ExtendedThread
{
    private static final float OPACITY_MIN = 0.7f;

    private NotificationWindow notificationWindow;
    private long delay;

    public NotificationWindowFlashThread(NotificationWindow notificationWindow, long delay)
    {
        this.notificationWindow = notificationWindow;
        this.delay = delay;
    }

    @Override
    public void run()
    {
        final float AMOUNT_STEP = 0.05f;

        boolean additiveOpacity = false;
        float newOpacity;

        while (!isExit())
        {
            // Change opacity
            if (additiveOpacity)
            {
                newOpacity = notificationWindow.getOpacity() + AMOUNT_STEP;
            }
            else
            {
                newOpacity = notificationWindow.getOpacity() - AMOUNT_STEP;
            }

            // Clamp opacity
            if (newOpacity < OPACITY_MIN)
            {
                newOpacity = OPACITY_MIN;
                additiveOpacity = true;
            }
            else if (newOpacity > 1.0f)
            {
                newOpacity = 1.0f;
                additiveOpacity = false;
            }

            // Set new opacity
            notificationWindow.setOpacity(newOpacity);

            // Delay before next step
            try
            {
                Thread.sleep(delay);
            }
            catch (InterruptedException ex) {}
        }
    }

}
