package com.limpygnome.ws281x.daemon.service.api;

import com.limpygnome.ws281x.daemon.Controller;

/**
 * Created by limpygnome on 18/07/15.
 */
public interface Service
{
    void start(Controller controller);

    void stop(Controller controller);
}
