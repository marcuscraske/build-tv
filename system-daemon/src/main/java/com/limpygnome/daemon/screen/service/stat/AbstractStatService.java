package com.limpygnome.daemon.screen.service.stat;

import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.screen.model.stat.Statistic;

/**
 * Used to implement a generic statistic service.
 *
 * This allows a system metric, or a measurable item, to be reported.
 */
public abstract class AbstractStatService implements Service
{

    long DEFAULT_PROCESS_TIMEOUT = 1000;

    public abstract Statistic update();

}
