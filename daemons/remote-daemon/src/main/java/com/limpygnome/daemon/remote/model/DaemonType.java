package com.limpygnome.daemon.remote.model;

/**
 * The type of local daemon.
 *
 * Used for matching the first path/directory of an incoming request to a local daemon port for forwarding/proxying.
 */
public enum DaemonType
{
    /**
     * The LED daemon - LED strip etc.
     */
    LED_DAEMON("led-daemon", "local-ports/led-daemon"),

    /**
     * The system daemon - statistics, screen, power etc.
     */
    SYSTEM_DAEMON("system-daemon", "local-ports/system-daemon"),

    /**
     * The build TV daemon - Jenkins etc.
     */
    BUILD_TV_DAEMON("build-tv-daemon", "local-ports/build-tv-daemon"),

    /**
     * The interval daemon - timed patterns/notifications etc.
     */
    INTERVAL_DAEMON("interval-daemon", "local-ports/interval-daemon")
    ;

    public final String TOP_LEVEL_PATH;
    public final String SETTING_KEY_PORT;

    DaemonType(String TOP_LEVEL_PATH, String SETTING_KEY_PORT)
    {
        this.TOP_LEVEL_PATH = TOP_LEVEL_PATH;
        this.SETTING_KEY_PORT = SETTING_KEY_PORT;
    }

}
