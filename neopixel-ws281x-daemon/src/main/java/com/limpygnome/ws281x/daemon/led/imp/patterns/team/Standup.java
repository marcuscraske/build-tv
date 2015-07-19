package com.limpygnome.ws281x.daemon.led.imp.patterns.team;

import com.limpygnome.ws281x.daemon.led.api.Pattern;
import com.limpygnome.ws281x.daemon.led.imp.LedController;
import com.limpygnome.ws281x.daemon.led.imp.LedRenderThread;
import com.limpygnome.ws281x.daemon.led.imp.patterns.template.GenericFlyInPattern;
import com.limpygnome.ws281x.daemon.led.imp.patterns.template.GenericPulsePattern;

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
