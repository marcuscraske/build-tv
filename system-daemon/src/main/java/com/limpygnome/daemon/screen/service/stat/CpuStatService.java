package com.limpygnome.daemon.screen.service.stat;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.screen.model.stat.Statistic;
import com.limpygnome.daemon.screen.service.StatsService;

/**
 * Created by limpygnome on 16/10/15.
 */
public class CpuStatService implements AbstractStatService
{
    private static final String LABEL = "CPU";


    @Override
    public void start(Controller controller)
    {
        // Load thresholds
    }

    @Override
    public void stop(Controller controller)
    {
    }

    @Override
    public Statistic update()
    {
        final String BASH_COMMANDS = "grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage}'";

        float value = statsService.getEnvironmentService().execute(BASH_COMMANDS, DEFAULT_PROCESS_TIMEOUT);
        float min;
        float max;

        return new Statistic(LABEL, min, max, value);
    }
}
