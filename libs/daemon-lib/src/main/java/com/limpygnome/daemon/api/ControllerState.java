package com.limpygnome.daemon.api;

/**
 * Created by limpygnome on 20/10/15.
 */
public enum ControllerState
{
    STARTING(0),
    RUNNING(1),
    STOPPING(2),
    STOPPED(3)
    ;

    public int LIFECYCLE_STEP;

    ControllerState(int LIFECYCLE_STEP)
    {
        this.LIFECYCLE_STEP = LIFECYCLE_STEP;
    }
}
