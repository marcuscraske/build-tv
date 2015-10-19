package com.limpygnome.daemon.system.service.stat;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.system.model.stat.Statistic;

/**
 * Created by limpygnome on 19/10/15.
 */
public class TemperatureStatService extends AbstractStatService
{
    public static final String SERVICE_NAME = "stats_temp";

    private float min;
    private float max;

    @Override
    public void start(Controller controller)
    {
        super.start(controller);

        // Setup min/max thresholds
        min = controller.getSettings().getOptionalFloat("stats/temperature/min", 20.0f);
        max = controller.getSettings().getOptionalFloat("stats/temperature/max", 100.0f);
    }

    @Override
    public Statistic update()
    {
        final String[] BASH_COMMANDS = { "/bin/sh", "-c", "expr substr \"$(cat /sys/class/thermal/thermal_zone0/temp)\" 1 2" };

        // Fetch value
        float value;

        if (environmentService != null)
        {
            value = environmentService.execute(BASH_COMMANDS, DEFAULT_PROCESS_TIMEOUT);
        }
        else
        {
            value = max;
        }

        // Check if value exceeds thresholds
        if (value > max)
        {
            max = value;
        }
        else if (value < min)
        {
            min = value;
        }

        return new Statistic("Temperature", min, max, value);
    }

}
