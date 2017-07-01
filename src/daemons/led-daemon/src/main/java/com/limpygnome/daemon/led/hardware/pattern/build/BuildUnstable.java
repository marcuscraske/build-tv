package com.limpygnome.daemon.led.hardware.pattern.build;

import com.limpygnome.daemon.led.hardware.controller.LedController;
import com.limpygnome.daemon.led.hardware.pattern.Pattern;
import com.limpygnome.daemon.led.hardware.LedRenderThread;
import com.limpygnome.daemon.led.hardware.pattern.template.GenericKnightRiderPattern;

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

    @Override
    public String getName()
    {
        return "build-unstable";
    }
}
