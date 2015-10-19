package com.limpygnome.daemon.system.service.stat;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.system.model.stat.Statistic;

/**
 * Created by limpygnome on 19/10/15.
 */
public class RamStatService extends AbstractStatService
{
    public static final String SERVICE_NAME = "stats_ram";

    private static final String BASH_COMMANDS_MAX_MEMORY = "free -m| grep  Mem | awk '{ print int($2) }'";
    private static final String BASH_COMMANDS_MEMORY_USED = "free -m| grep  Mem | awk '{ print int($3) }'";
    //private static final String BASH_COMMANDS_MEMORY_PERCENT = "free -m| grep  Mem | awk '{ print int($3/$2*100) }'";

    private float maxMemory;

    @Override
    public void start(Controller controller)
    {
        super.start(controller);

        // Fetch max memory available
//        maxMemory = environmentService.execute(BASH_COMMANDS_MAX_MEMORY, DEFAULT_PROCESS_TIMEOUT);
        maxMemory = environmentService.execute("free -m | grep Mem", DEFAULT_PROCESS_TIMEOUT);
//        maxMemory = environmentService.execute("ifconfig", DEFAULT_PROCESS_TIMEOUT);
    }

    @Override
    public Statistic update()
    {
        float value;
        float min = 0.0f;

        if (environmentService != null)
        {
            value = environmentService.execute(BASH_COMMANDS_MEMORY_USED, DEFAULT_PROCESS_TIMEOUT);
        }
        else
        {
            value = 0.0f;
        }

        return new Statistic("RAM", min, maxMemory, value);
    }

}
