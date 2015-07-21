package com.limpygnome.daemon.ws281x.led.pattern.build;

import com.limpygnome.daemon.ws281x.led.Pattern;
import com.limpygnome.daemon.ws281x.led.LedController;
import com.limpygnome.daemon.ws281x.led.LedRenderThread;
import com.limpygnome.daemon.ws281x.led.pattern.template.GenericKnightRiderPattern;

/**
 * Created by limpygnome on 18/07/15.
 */
public class BuildUnstable implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        GenericKnightRiderPattern.renderKnightRiderEffect(ledRenderThread, ledController, 30, 255, 255, 0);
    }
}
