package com.limpygnome.daemon.system.service.stat;

import com.limpygnome.daemon.system.model.stat.Statistic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by limpygnome on 16/10/15.
 */
public class CpuStatService extends AbstractStatService
{
    private static final Logger LOG = LogManager.getLogger(CpuStatService.class);

    public static final String SERVICE_NAME = "stats_cpu";

    private static final String LABEL = "CPU";

    @Override
    public Statistic update()
    {
        final String[] BASH_COMMANDS = { "/bin/sh", "-c", "grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage}'" };

        Float value;
        float min = 0.0f;
        float max = 100.0f;

        if (environmentService != null)
        {
            value = environmentService.execute(BASH_COMMANDS, DEFAULT_PROCESS_TIMEOUT);

            if (value == null)
            {
                LOG.warn("Failed to retrieve CPU usage, possibly unsupported");
                value = 0.0f;
            }

            // Round value
            value = (float) Math.round(value);

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
