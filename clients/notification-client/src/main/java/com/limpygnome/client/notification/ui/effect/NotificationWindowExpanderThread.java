package com.limpygnome.client.notification.ui.effect;

import com.limpygnome.client.notification.ui.NotificationWindow;
import com.limpygnome.daemon.common.ExtendedThread;

import com.limpygnome.daemon.util.EnvironmentUtil;
import java.awt.*;

/**
 * A UI effect to gradually expand the notification window.
 */
public class NotificationWindowExpanderThread extends ExtendedThread
{
    private NotificationWindow notificationWindow;
    private int targetWidth;
    private int targetHeight;
    private long delay;

    public NotificationWindowExpanderThread(NotificationWindow notificationWindow, long delay)
    {
        this.notificationWindow = notificationWindow;
        this.delay = delay;

        // Compute target size
        Dimension screenSize = EnvironmentUtil.getScreenSize();
        this.targetWidth = (int) (screenSize.getWidth() * 0.8);
        this.targetHeight = (int) (screenSize.getHeight() * 0.8);
    }

    @Override
    public void run()
    {
        final int INCREMENT_PER_STEP = 100;

        int windowWidth, windowHeight;
        int incrementAmount;

        long start, totalTime;

        do
        {
            windowWidth = notificationWindow.getWidth();
            windowHeight = notificationWindow.getHeight();

            start = System.currentTimeMillis();

            // Increment window size
            if (windowWidth < targetWidth)
            {
                incrementAmount = INCREMENT_PER_STEP;

                if (windowWidth + incrementAmount > targetWidth)
                {
                    incrementAmount = targetWidth - windowWidth;
                }

                notificationWindow.setSize(windowWidth + incrementAmount, windowHeight);
            }
            else if (windowHeight < targetHeight)
            {
                incrementAmount = INCREMENT_PER_STEP;

                if (windowHeight + incrementAmount > targetHeight)
                {
                    incrementAmount = targetHeight - windowHeight;
                }

                notificationWindow.setSize(windowWidth, windowHeight + incrementAmount);
            }

            // Ensure window centered
            notificationWindow.centerOnScreen();

            // Delay before continuing
            try
            {
                totalTime = System.currentTimeMillis() - start;

                if (totalTime <= delay)
                {
                    Thread.sleep(delay - totalTime);
                }
            }
            catch (InterruptedException e) { }
        }
        while (!isExit() && (windowWidth < targetWidth || windowHeight < targetHeight));

        // Show message since resize complete
        notificationWindow.showMessage();
    }

}
