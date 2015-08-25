package com.limpygnome.daemon.led.hardware.pattern.daemon;

import com.limpygnome.daemon.led.hardware.controller.LedController;
import com.limpygnome.daemon.led.hardware.pattern.Pattern;
import com.limpygnome.daemon.led.hardware.LedRenderThread;

/**
 * Created by limpygnome on 18/07/15.
 */
public class Startup implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        // Gradually wake lights up
        for (int i = 0; i <= 255 && !ledRenderThread.isExit(); i++)
        {
            ledController.setStrip(i, i, i);
            ledController.render();

            Thread.sleep(100);
        }
    }

    @Override
    public String getName()
    {
        return "startup";
    }
}
