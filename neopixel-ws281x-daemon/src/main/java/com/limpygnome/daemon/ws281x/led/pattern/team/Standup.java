package com.limpygnome.daemon.ws281x.led.pattern.team;

import com.limpygnome.daemon.ws281x.led.Pattern;
import com.limpygnome.daemon.ws281x.led.LedController;
import com.limpygnome.daemon.ws281x.led.LedRenderThread;
import com.limpygnome.daemon.ws281x.led.pattern.template.GenericFlyInPattern;
import com.limpygnome.daemon.ws281x.led.pattern.template.GenericPulsePattern;

/**
 * Created by limpygnome on 18/07/15.
 */
public class Standup implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        final int red = 255;
        final int green = 20;
        final int blue = 147;

        while (!ledRenderThread.isExit())
        {
            GenericFlyInPattern.flyIn(ledRenderThread, ledController, red, green, blue, 40);
            GenericPulsePattern.pulse(ledRenderThread, ledController, red, green, blue, 100, 0.05f, 100);
        }
    }
}
