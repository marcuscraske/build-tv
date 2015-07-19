package com.limpygnome.daemon.api;

/**
 * Created by limpygnome on 18/07/15.
 */
public interface Service
{
    void start(Controller controller);

    void stop(Controller controller);
}
