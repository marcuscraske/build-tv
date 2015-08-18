package com.limpygnome.daemon.led.hardware.pattern.build;

import com.limpygnome.daemon.led.hardware.pattern.template.GenericKnightRiderPattern;
import com.limpygnome.daemon.led.hardware.Pattern;
import com.limpygnome.daemon.led.hardware.LedController;
import com.limpygnome.daemon.led.hardware.LedRenderThread;

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
