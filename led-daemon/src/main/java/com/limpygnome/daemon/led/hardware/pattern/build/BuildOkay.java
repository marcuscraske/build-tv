package com.limpygnome.daemon.led.hardware.pattern.build;

import com.limpygnome.daemon.led.hardware.controller.LedController;
import com.limpygnome.daemon.led.hardware.pattern.Pattern;
import com.limpygnome.daemon.led.hardware.LedRenderThread;

/**
 * Created by limpygnome on 18/07/15.
 */
public class BuildOkay implements Pattern
{
    /**
     * The pattern is updated at this frequency.
     *
     * This is so that if the LEDs are unpowered, they will be updated again at some point in the future.
     */
    private static final long IDLE_TIME = 10000;

    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        while (!ledRenderThread.isExit())
        {
            ledController.setStrip(0, 255, 0);
            ledController.render();

            Thread.sleep(IDLE_TIME);
        }
    }

    @Override
    public String getName()
    {
        return "build-ok";
    }
}
