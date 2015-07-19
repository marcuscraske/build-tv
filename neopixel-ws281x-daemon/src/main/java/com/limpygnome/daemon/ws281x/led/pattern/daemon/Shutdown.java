package com.limpygnome.daemon.ws281x.led.pattern.daemon;

import com.limpygnome.daemon.ws281x.led.Pattern;
import com.limpygnome.daemon.ws281x.led.LedController;
import com.limpygnome.daemon.ws281x.led.LedRenderThread;

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

            Thread.sleep(5);
        }
    }
}
