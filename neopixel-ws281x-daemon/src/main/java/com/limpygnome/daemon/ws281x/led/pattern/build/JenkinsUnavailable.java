package com.limpygnome.daemon.ws281x.led.pattern.build;

import com.limpygnome.daemon.ws281x.led.Pattern;
import com.limpygnome.daemon.ws281x.led.LedController;
import com.limpygnome.daemon.ws281x.led.LedRenderThread;
import com.limpygnome.daemon.ws281x.led.pattern.template.GenericPulsePattern;

/**
 * Created by limpygnome on 18/07/15.
 */
public class JenkinsUnavailable implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        GenericPulsePattern.pulse(ledRenderThread, ledController, 255, 0, 0, 40, 0.05f);
    }
}
