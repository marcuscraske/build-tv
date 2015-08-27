package com.limpygnome.daemon.buildtv.led.pattern;

/**
 * The LED pattern to use to reflect the status of the build server. Pattern with highest priority  should be used.
 */
public enum LedPattern
{
    BUILD_UNKNOWN(0, "build-unknown"),
    BUILD_OK(1, "build-ok"),
    BUILD_UNSTABLE(2, "build-unstable"),
    BUILD_FAILURE(3, "build-failure"),
    BUILD_PROGRESS(4, "build-progress"),
    JENKINS_UNAVAILABLE(5, "jenkins-unavailable"),

    STARTUP(0, "startup"),
    SHUTDOWN(999, "shutdown"),

    STANDUP(500, "standup")
    ;

    public final int PRIORITY;
    public final String PATTERN;

    LedPattern(int priority, String pattern)
    {
        this.PRIORITY = priority;
        this.PATTERN = pattern;
    }

    public static LedPattern getByName(String name)
    {
        for (LedPattern ledPattern : values())
        {
            if (ledPattern.PATTERN.equals(name))
            {
                return ledPattern;
            }
        }

        throw new RuntimeException("Unknown LED pattern '" + name + "'");
    }

}
