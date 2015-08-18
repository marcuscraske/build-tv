package com.limpygnome.daemon.led.hardware.pattern.team;

import com.limpygnome.daemon.led.hardware.Pattern;
import com.limpygnome.daemon.led.hardware.LedController;
import com.limpygnome.daemon.led.hardware.LedRenderThread;
import com.limpygnome.daemon.led.hardware.pattern.template.GenericFlyInPattern;
import com.limpygnome.daemon.led.hardware.pattern.template.GenericPulsePattern;

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
            GenericPulsePattern.pulse(ledRenderThread, ledController, red, green, blue, 40, 0.05f, 40);
        }
    }
}
