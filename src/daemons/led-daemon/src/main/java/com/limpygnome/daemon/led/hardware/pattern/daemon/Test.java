package com.limpygnome.daemon.led.hardware.pattern.daemon;

import com.limpygnome.daemon.led.hardware.LedRenderThread;
import com.limpygnome.daemon.led.hardware.controller.LedController;
import com.limpygnome.daemon.led.hardware.pattern.Pattern;
import com.limpygnome.daemon.led.hardware.pattern.template.GenericFlyInPattern;
import com.limpygnome.daemon.led.hardware.pattern.template.GenericKnightRiderPattern;
import com.limpygnome.daemon.led.hardware.pattern.template.GenericPulsePattern;
import com.limpygnome.daemon.led.hardware.pattern.template.GenericRainbowPattern;

/**
 * A test pattern for diagnosing LED strip issues.
 */
public class Test implements Pattern
{
    @Override
    public void render(LedRenderThread ledRenderThread, LedController ledController) throws InterruptedException
    {
        int totalLeds = ledController.getLedsCount();

        // Turn all LEDs off
        ledController.setStrip(0, 0, 0);
        Thread.sleep(500);
        ledController.render();

        // Basic LED setting with white
        for (int i = 0; i < totalLeds; i++)
        {
            ledController.setPixel(i, 255, 255, 255);
            ledController.render();
            Thread.sleep(200);
        }

        Thread.sleep(2000);

        // Test each R G B
        ledController.setStrip(255, 0, 0);
        ledController.render();
        Thread.sleep(2000);

        ledController.setStrip(0, 255, 0);
        ledController.render();
        Thread.sleep(2000);

        ledController.setStrip(0, 0, 255);
        ledController.render();
        Thread.sleep(2000);

        // Test basic flyin
        for (int i = 0; i < 5; i++)
        {
            GenericFlyInPattern.flyIn(ledRenderThread, ledController, 255, 255, 255, 60);
        }

        // Test basic knight rider
        GenericKnightRiderPattern.renderKnightRiderEffect(ledRenderThread, ledController, 60, 255, 255, 255, 100);

        // Test basic pulse
        GenericPulsePattern.pulse(ledRenderThread, ledController, 255, 255, 255, 60, 0.05f, 100);

        // Infinite rainbow...
        GenericRainbowPattern.rainbow(ledRenderThread, ledController, 20);
    }

    @Override
    public String getName()
    {
        return "test";
    }
}
