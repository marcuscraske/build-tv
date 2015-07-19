package com.limpygnome.daemon.buildtv.led;

/**
 * Created by limpygnome on 19/07/15.
 */
public enum LedPattern
{
    BUILD_UNKNOWN(0, "build-unknown"),
    BUILD_OK(1, "build-ok"),
    BUILD_PROGRESS(2, "build-progress"),
    BUILD_UNSTABLE(3, "build-unstable"),
    BUILD_FAILURE(4, "build-failure"),
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
