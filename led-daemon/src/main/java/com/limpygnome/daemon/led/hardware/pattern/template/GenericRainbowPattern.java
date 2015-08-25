package com.limpygnome.daemon.led.hardware.pattern.template;

import com.limpygnome.daemon.led.hardware.LedRenderThread;
import com.limpygnome.daemon.led.hardware.controller.LedController;

/**
 * Provides generic rainbow pattern; credit for this goes to the following:
 * https://github.com/richardghirst/rpi_ws281x/blob/master/python/examples/strandtest.py
 */
public class GenericRainbowPattern
{
    public static void rainbow(LedRenderThread ledRenderThread, LedController ledController, long frameDelay) throws InterruptedException
    {
        rainbow(ledRenderThread, ledController, frameDelay, -1);
    }
    public static void rainbow(LedRenderThread ledRenderThread, LedController ledController, long frameDelay, int iterations) throws InterruptedException
    {
        int ledCount = ledController.getLedsCount();

        int[] value;
        int iteration = 0;

        while (!ledRenderThread.isExit() && (iterations == -1 || iteration <= iterations))
        {
            for (int j = 0; j < ledCount; j++)
            {
                value = wheel(
                        ((iteration * 256) / ledCount) + j
                );

                ledController.setPixel(j, value[0] & 255, value[1] & 255, value[2] & 255);
            }

            ledController.render();
            Thread.sleep(frameDelay);

            iteration++;
        }
    }

    private static int[] wheel(int pos)
    {
        if (pos < 85)
        {
            return new int[] {
                    pos * 3,
                    255 - pos * 3,
                    0
            };
        }
        else if (pos < 170)
        {
            pos -= 85;

            return new int[] {
                    255 - pos * 3,
                    0,
                    pos * 3
            };
        }
        else
        {
            pos -= 170;

            return new int[] {
                    0,
                    pos * 3,
                    255 - pos * 3
            };
        }
    }
}
