package com.limpygnome.daemon.system.service.stat;

import com.limpygnome.daemon.system.model.stat.Statistic;

/**
 * Created by limpygnome on 16/10/15.
 */
public class CpuStatService extends AbstractStatService
{
    public static final String SERVICE_NAME = "stats_cpu";

    private static final String LABEL = "CPU";

    @Override
    public Statistic update()
    {
        final String[] BASH_COMMANDS = { "/bin/sh", "-c", "grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage}'" };

        float value;
        float min = 0.0f;
        float max = 100.0f;

        if (environmentService != null)
        {
            value = environmentService.execute(BASH_COMMANDS, DEFAULT_PROCESS_TIMEOUT);
            min = 0.0f;
            max = 100.0f;
        }
        else
        {
            value = 0.0f;
        }


        return new Statistic(LABEL, min, max, value, "PERCENTAGE");
    }

}
