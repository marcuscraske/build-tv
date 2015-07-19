package com.limpygnome.ws281x.daemon.led.imp.patterns.template;

import com.limpygnome.ws281x.daemon.led.imp.LedController;
import com.limpygnome.ws281x.daemon.led.imp.LedRenderThread;

/**
 * Pulses a colour with a delay.
 */
public class GenericPulsePattern
{

    public static void pulse(LedRenderThread ledRenderThread, LedController ledController, int red, int green, int blue,
                             long frameDelay, float pulseStepMultiplier) throws InterruptedException
    {
        pulse(ledRenderThread, ledController, red, green, blue, frameDelay, pulseStepMultiplier, -1);
    }

    /**
     *
     * @param ledRenderThread
     * @param ledController
     * @param red
     * @param green
     * @param blue
     * @param frameDelay
     * @param pulseStepMultiplier
     * @param maxIterations Maximum iterations, or less than zero for unlimited
     * @throws InterruptedException
     */
    public static void pulse(LedRenderThread ledRenderThread, LedController ledController, int red, int green, int blue,
                             long frameDelay, float pulseStepMultiplier, int maxIterations) throws InterruptedException
    {
        final float PULSE_MAX = 1.0f;
        final float PULSE_MIN = 0.4f;

        // Pulse RGB
        boolean positive = true;
        float pulseMultiplier = PULSE_MIN;
        int ledRed, ledGreen, ledBlue;
        int iterations = 0;

        while (!ledRenderThread.isExit())
        {
            // Compute values for current pulse frame
            ledRed = (int) ( (float) red * pulseMultiplier);
            ledGreen = (int) ( (float) green * pulseMultiplier);
            ledBlue = (int) ( (float) blue * pulseMultiplier );

            // Set strip and render
            ledController.setStrip(ledRed, ledGreen, ledBlue);
            ledController.render();

            // Prepare values for next frame
            pulseMultiplier += positive ? pulseStepMultiplier : pulseStepMultiplier * -1.0f;

            if (pulseMultiplier < PULSE_MIN)
            {
                positive = true;
                pulseMultiplier = PULSE_MIN;
            }
            else if (pulseMultiplier > PULSE_MAX)
            {
                positive = false;
                pulseMultiplier = PULSE_MAX;
            }

            // Check iterations limit
            if (maxIterations > 0 && iterations++ > maxIterations)
            {
                return;
            }

            // Frame finished, sleep
            Thread.sleep(frameDelay);
        }
    }

}
