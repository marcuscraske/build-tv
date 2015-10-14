package com.limpygnome.daemon.remote.model;

/**
 * The type of local daemon.
 */
public enum DaemonType
{
    LED_DAEMON("led-daemon", "local-ports/led-daemon"),
    SYSTEM_DAEMON("system-daemon", "local-ports/system-daemon"),
    BUILD_TV_DAEMON("build-tv-daemon", "local-ports/build-tv-daemon")
    ;

    public final String TOP_LEVEL_PATH;
    public final String SETTING_KEY_PORT;

    DaemonType(String TOP_LEVEL_PATH, String SETTING_KEY_PORT)
    {
        this.TOP_LEVEL_PATH = TOP_LEVEL_PATH;
        this.SETTING_KEY_PORT = SETTING_KEY_PORT;
    }

}
