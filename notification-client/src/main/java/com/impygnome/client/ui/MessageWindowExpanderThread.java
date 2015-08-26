package com.impygnome.client.ui;

import java.awt.*;

/**
 * Created by limpygnome on 26/08/15.
 */
public class MessageWindowExpanderThread extends Thread
{
    private MessageWindow messageWindow;
    private int targetWidth;
    private int targetHeight;
    private long delay;

    public MessageWindowExpanderThread(MessageWindow messageWindow, long delay)
    {
        this.messageWindow = messageWindow;
        this.delay = delay;

        // Compute target size
        Dimension screenSize = messageWindow.getScreenSize();
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
            windowWidth = messageWindow.getWidth();
            windowHeight = messageWindow.getHeight();

            start = System.currentTimeMillis();

            // Increment window size
            if (windowWidth < targetWidth)
            {
                incrementAmount = INCREMENT_PER_STEP;

                if (windowWidth + incrementAmount > targetWidth)
                {
                    incrementAmount = targetWidth - windowWidth;
                }

                messageWindow.setSize(windowWidth + incrementAmount, windowHeight);
            }
            else if (windowHeight < targetHeight)
            {
                incrementAmount = INCREMENT_PER_STEP;

                if (windowHeight + incrementAmount > targetHeight)
                {
                    incrementAmount = targetHeight - windowHeight;
                }

                messageWindow.setSize(windowWidth, windowHeight + incrementAmount);
            }

            // Ensure window centered
            messageWindow.centerOnScreen();

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
        while (windowWidth < targetWidth || windowHeight < targetHeight);

        // Show message since resize complete
        messageWindow.showMessage();
    }

}
