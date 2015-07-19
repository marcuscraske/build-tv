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
    JENKINS_UNAVAILABLE(5, "jenkins-unavailable")
    ;

    public final int PRIORITY;
    public final String PATTERN;

    LedPattern(int priority, String pattern)
    {
        this.PRIORITY = priority;
        this.PATTERN = pattern;
    }

}
