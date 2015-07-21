package com.limpygnome.daemon.ws281x.led.pattern.build;

import com.limpygnome.daemon.ws281x.led.pattern.template.GenericKnightRiderPattern;
import com.limpygnome.daemon.ws281x.led.Pattern;
import com.limpygnome.daemon.ws281x.led.LedController;
import com.limpygnome.daemon.ws281x.led.LedRenderThread;

/**
 * Created by limpygnome on 19/07/15.
 */
public class BuildFailure implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        GenericKnightRiderPattern.renderKnightRiderEffect(ledRenderThread, ledController, 20, 255, 0, 0);
    }
}
