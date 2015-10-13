package com.limpygnome.daemon.remote.model;

/**
 * The type of local daemon.
 */
public enum DaemonType
{
    LED_DAEMON("local-ports/led-daemon"),
    SCREEN_DAEMON("local-ports/screen-daemon"),
    BUILD_TV_DAEMON("local-ports/build-tv-daemon")
    ;

    public final String SETTING_KEY_PORT;

    DaemonType(String SETTING_KEY_PORT)
    {
        this.SETTING_KEY_PORT = SETTING_KEY_PORT;
    }
}
