package com.impygnome.client.ui;

import com.limpygnome.daemon.common.ExtendedThread;

/**
 * Created by limpygnome on 26/08/15.
 */
public class MessageWindowFlashThread extends ExtendedThread
{
    private static final float OPACITY_MIN = 0.7f;

    private MessageWindow messageWindow;
    private long delay;

    public MessageWindowFlashThread(MessageWindow messageWindow, long delay)
    {
        this.messageWindow = messageWindow;
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
                newOpacity = messageWindow.getOpacity() + AMOUNT_STEP;
            }
            else
            {
                newOpacity = messageWindow.getOpacity() - AMOUNT_STEP;
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
            messageWindow.setOpacity(newOpacity);

            // Delay before next step
            try
            {
                Thread.sleep(delay);
            }
            catch (InterruptedException ex) {}
        }
    }

}
