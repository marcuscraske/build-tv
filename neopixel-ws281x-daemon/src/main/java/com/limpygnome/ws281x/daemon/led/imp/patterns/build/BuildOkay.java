package com.limpygnome.ws281x.daemon.led.imp.patterns.build;

import com.limpygnome.ws281x.daemon.led.api.Pattern;
import com.limpygnome.ws281x.daemon.led.imp.LedController;
import com.limpygnome.ws281x.daemon.led.imp.LedRenderThread;

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
