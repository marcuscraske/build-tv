package com.limpygnome.daemon.led.hardware.pattern;

import com.limpygnome.daemon.led.hardware.LedRenderThread;
import com.limpygnome.daemon.led.hardware.controller.LedController;
import com.limpygnome.daemon.led.hardware.controller.Ws281xLedController;

/**
 * Represents an (LED) pattern, which is used to render various sequences by setting the colour of LEDs.
 */
public interface Pattern
{

    void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException;

    String getName();

}
