package com.limpygnome.daemon.ws281x.led;

/**
 * Created by limpygnome on 18/07/15.
 */
public interface Pattern
{
    void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException;
}
