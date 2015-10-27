package com.limpygnome.daemon.led.hardware.pattern.daemon;

import com.limpygnome.daemon.led.hardware.LedRenderThread;
import com.limpygnome.daemon.led.hardware.controller.LedController;
import com.limpygnome.daemon.led.hardware.pattern.Pattern;
import com.limpygnome.daemon.led.hardware.pattern.template.GenericRainbowPattern;

/**
 * LED pattern for displaying an infinite rainbow.
 */
public class Rainbow implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        GenericRainbowPattern.rainbow(ledRenderThread, ledController, 20);
    }

    @Override
    public String getName()
    {
        return "rainbow";
    }
}
