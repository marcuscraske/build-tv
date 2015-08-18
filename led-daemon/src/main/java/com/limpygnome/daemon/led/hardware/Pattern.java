package com.limpygnome.daemon.led.hardware;

/**
 * Created by limpygnome on 18/07/15.
 */
public interface Pattern
{
    void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException;
}
