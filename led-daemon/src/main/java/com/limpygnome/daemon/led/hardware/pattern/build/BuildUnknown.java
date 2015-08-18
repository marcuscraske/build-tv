package com.limpygnome.daemon.led.hardware.pattern.build;

import com.limpygnome.daemon.led.hardware.LedController;
import com.limpygnome.daemon.led.hardware.LedRenderThread;
import com.limpygnome.daemon.led.hardware.Pattern;
import com.limpygnome.daemon.led.hardware.pattern.template.GenericPulsePattern;

/**
 * Created by limpygnome on 19/07/15.
 */
public class BuildUnknown implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        GenericPulsePattern.pulse(ledRenderThread, ledController, 255, 140, 0, 40, 0.01f);
    }
}
