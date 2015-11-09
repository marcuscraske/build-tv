package com.limpygnome.daemon.buildtv.led;

/**
 * Holds all of the available screen options/actions.
 */
public enum ScreenAction
{
    ON("on"),
    OFF("off")
    ;

    public String ACTION;

    ScreenAction(String ACTION)
    {
        this.ACTION = ACTION;
    }
}
