package com.limpygnome.daemon.ws281x.led.pattern.build;

import com.limpygnome.daemon.ws281x.led.Pattern;
import com.limpygnome.daemon.ws281x.led.LedController;
import com.limpygnome.daemon.ws281x.led.LedRenderThread;

/**
 * Created by limpygnome on 18/07/15.
 */
public class BuildOkay implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        ledController.setStrip(0, 255, 0);
        ledController.render();
    }
}
