package com.limpygnome.daemon.system.service.stat;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.system.model.stat.Statistic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A stat service implementation to measure RAM usage.
 */
public class RamStatService extends AbstractStatService
{
    private static final Logger LOG = LogManager.getLogger(RamStatService.class);

    public static final String SERVICE_NAME = "stats_ram";

    private static final String[] BASH_COMMANDS_MAX_MEMORY = { "/bin/sh", "-c", "free -m| grep  Mem | awk '{ print int($2) }'" };
    private static final String[] BASH_COMMANDS_MEMORY_USED = { "/bin/sh", "-c", "free -m| grep  Mem | awk '{ print int($3) }'" };
    //private static final String BASH_COMMANDS_MEMORY_PERCENT = "free -m| grep  Mem | awk '{ print int($3/$2*100) }'";

    private float maxMemory;

    @Override
    public void start(Controller controller)
    {
        super.start(controller);

        // Fetch max memory available
        try
        {
            maxMemory = environmentService.execute(BASH_COMMANDS_MAX_MEMORY, DEFAULT_PROCESS_TIMEOUT);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to retrieve maximum system memory", e);
        }
    }

    @Override
    public Statistic update()
    {
        Float value;
        float min = 0.0f;

        if (environmentService != null)
        {
            value = environmentService.execute(BASH_COMMANDS_MEMORY_USED, DEFAULT_PROCESS_TIMEOUT);

            if (value == null)
            {
                LOG.warn("Failed to retrieve RAM usage, possibly unsupported");
                value = 0.0f;
            }
        }
        else
        {
            value = 0.0f;
        }

        return new Statistic("RAM", min, maxMemory, value, "MEGABYTES");
    }

}
