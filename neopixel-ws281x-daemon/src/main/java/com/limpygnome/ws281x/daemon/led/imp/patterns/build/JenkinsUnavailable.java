package com.limpygnome.ws281x.daemon.led.imp.patterns.build;

import com.limpygnome.ws281x.daemon.led.api.Pattern;
import com.limpygnome.ws281x.daemon.led.imp.LedController;
import com.limpygnome.ws281x.daemon.led.imp.LedRenderThread;
import com.limpygnome.ws281x.daemon.led.imp.patterns.template.GenericPulsePattern;

/**
 * Created by limpygnome on 18/07/15.
 */
public class JenkinsUnavailable implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        GenericPulsePattern.pulse(ledRenderThread, ledController, 255, 0, 0, 100, 0.05f);
    }
}
