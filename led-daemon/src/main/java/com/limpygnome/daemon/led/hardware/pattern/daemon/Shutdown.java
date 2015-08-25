package com.limpygnome.daemon.led.hardware.pattern.daemon;

import com.limpygnome.daemon.led.hardware.controller.LedController;
import com.limpygnome.daemon.led.hardware.pattern.Pattern;
import com.limpygnome.daemon.led.hardware.LedRenderThread;

/**
 * Created by limpygnome on 18/07/15.
 */
public class Shutdown implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        // Gradually fade lights down
        for (int i = 255; i >= 0; i--)
        {
            ledController.setStrip(i, i, i);
            ledController.render();
            try
            {
                Thread.sleep(5);
            }
            catch (InterruptedException e)
            {
                // We don't care, we want to hang in this thread for fade effect
            }
        }
    }

    @Override
    public String getName()
    {
        return "Shutdown";
    }
}
