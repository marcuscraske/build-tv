package com.limpygnome.daemon.led.hardware.controller;

/**
 * Used by implementations of LED controllers.
 */
public interface LedController
{

    /**
     * Sets all of the LEDs in the strip to the specified colour.
     *
     * @param red 0 to 255
     * @param green 0 to 255
     * @param blue 0 to 255
     */
    void setStrip(int red, int green, int blue);

    /**
     * Sets a pixel to the specified colour.
     *
     * @param pixel The pixel index, from 0 to n-1
     * @param red 0 to 255
     * @param green 0 to 255
     * @param blue 0 to 255
     */
    void setPixel(int pixel, int red, int green, int blue);

    /**
     * Renders the LED pattern set.
     */
    void render();

    /**
     * Disposes the underlying hardware.
     */
    void dispose();

    /**
     * The number of LEDs attached.
     *
     * @return number of LEDs
     */
    int getLedsCount();

}
