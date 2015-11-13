package com.limpygnome.daemon.system.service.stat;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.system.model.stat.Statistic;
import com.limpygnome.daemon.util.EnvironmentUtil;
import com.limpygnome.daemon.util.MathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A stat service implementation for measuring the CPU temperature.
 */
public class TemperatureStatService extends AbstractStatService
{
    private static final Logger LOG = LogManager.getLogger(TemperatureStatService.class);

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
        Float value = EnvironmentUtil.execFloat(BASH_COMMANDS, DEFAULT_PROCESS_TIMEOUT, false);

        if (value == null)
        {
            LOG.warn("Failed to retrieve temperature, possibly unsupported");
            value = 0.0f;
        }

        // Round value
        value = MathUtil.round(value, 2);

        // Check if value exceeds thresholds
        if (value > max)
        {
            max = value;
        }
        else if (value < min)
        {
            min = value;
        }

        return new Statistic("Temperature", min, max, value, "CELSIUS");
    }

}
