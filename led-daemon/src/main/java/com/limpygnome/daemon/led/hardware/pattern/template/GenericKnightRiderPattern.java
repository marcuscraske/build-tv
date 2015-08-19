package com.limpygnome.daemon.led.hardware.pattern.template;

import com.limpygnome.daemon.led.hardware.controller.LedController;
import com.limpygnome.daemon.led.hardware.LedRenderThread;

/**
 * Renders a knight rider effect for a chosen RGB value and delay.
 */
public class GenericKnightRiderPattern
{

    public static void renderKnightRiderEffect(LedRenderThread ledRenderThread, LedController ledController,
                                               long frameDelay, int red, int green, int blue) throws InterruptedException
    {
        int currentLedIndex = 0;
        boolean positiveFlow = true;

        int ledsCount = ledController.getLedsCount();
        int maxFallOffLeds = 16;
        int maxRange = ledsCount + (maxFallOffLeds / 2);
        int minRange = -(maxFallOffLeds / 2);

        while (!ledRenderThread.isExit())
        {
            // Render frame
            knightRiderFrame(ledController, currentLedIndex, maxFallOffLeds, ledsCount, red, green, blue);

            // Increment current led
            currentLedIndex += positiveFlow ? 1 : -1;

            if (currentLedIndex < minRange)
            {
                positiveFlow = true;
                currentLedIndex = minRange;
            }
            else if (currentLedIndex >= maxRange)
            {
                positiveFlow = false;
                currentLedIndex = maxRange - 1;
            }

            // Sleep...
            Thread.sleep(frameDelay);
        }
    }

    private static void knightRiderFrame(LedController ledController, int currentLedIndex, int maxFallOffLeds,
                                         int ledsCount, int red, int green, int blue)
    {
        final float DAMPEN_MULTIPLIER = 0.05f;

        // Compute max/min/diff values
        float maxRed = red;
        float maxGreen = green;
        float maxBlue = blue;

        float minRed = maxRed * DAMPEN_MULTIPLIER;
        float minGreen = maxGreen * DAMPEN_MULTIPLIER;
        float minBlue = maxBlue * DAMPEN_MULTIPLIER;

        float diffRed = maxRed - minRed;
        float diffGreen = maxGreen - minGreen;
        float diffBlue = maxBlue - minBlue;

        // Compute fall off for each LED
        float indexDistance;
        float fallOffMultiplier;
        float ledRed, ledGreen, ledBlue;

        for (int i = 0; i < ledsCount; i++)
        {
            indexDistance = Math.abs(currentLedIndex - i);

            // Check if fall-off led
            if (indexDistance < maxFallOffLeds)
            {
                // Compute fall-off multiplier
                fallOffMultiplier = ( (float) maxFallOffLeds - indexDistance) / (float) maxFallOffLeds;

                // Compute RGB
                ledRed = minRed + (fallOffMultiplier * diffRed);
                ledGreen = minGreen + (fallOffMultiplier * diffGreen);
                ledBlue = minBlue + (fallOffMultiplier * diffBlue);
            }
            else
            {
                // Minimum values
                ledRed = minRed;
                ledGreen = minGreen;
                ledBlue = minBlue;
            }

            ledController.setPixel(i, (int) ledRed, (int) ledGreen, (int) ledBlue);
        }

        ledController.render();
    }

}
