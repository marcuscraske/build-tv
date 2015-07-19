package com.limpygnome.ws281x.daemon.led.imp.patterns.daemon;

import com.limpygnome.ws281x.daemon.led.api.Pattern;
import com.limpygnome.ws281x.daemon.led.imp.LedController;
import com.limpygnome.ws281x.daemon.led.imp.LedRenderThread;

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
}
