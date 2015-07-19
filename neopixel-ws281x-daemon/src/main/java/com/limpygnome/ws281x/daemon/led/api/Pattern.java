package com.limpygnome.ws281x.daemon.led.api;

import com.limpygnome.ws281x.daemon.led.imp.LedController;
import com.limpygnome.ws281x.daemon.led.imp.LedRenderThread;

/**
 * Created by limpygnome on 18/07/15.
 */
public interface Pattern
{
    void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException;
}
