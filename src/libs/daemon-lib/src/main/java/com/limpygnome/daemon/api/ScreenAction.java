package com.limpygnome.daemon.api;

/**
 * Holds all of the available options/actions for the screen/VHD connected to the system.
 */
public enum ScreenAction
{
    /**
     * Used to turn the screen on.
     */
    ON("on"),

    /**
     * Used to turn the screen off.
     */
    OFF("off")
    ;

    public String ACTION;

    ScreenAction(String ACTION)
    {
        this.ACTION = ACTION;
    }

}
