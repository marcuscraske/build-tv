package com.limpygnome.daemon.system;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.imp.DefaultController;
import com.limpygnome.daemon.system.service.*;
import com.limpygnome.daemon.service.RestService;
import com.limpygnome.daemon.system.service.stat.CpuStatService;
import com.limpygnome.daemon.system.service.stat.RamStatService;
import com.limpygnome.daemon.system.service.stat.TemperatureStatService;

/**
 * Entry point into the system daemon.
 */
public class ProgramSystem
{

    public static void main(String[] args)
    {
        Controller controller = new DefaultController("system-daemon");

        // Add services
        controller.add(ScreenService.SERVICE_NAME, new ScreenService());
        controller.add(PowerManagementService.SERVICE_NAME, new PowerManagementService());
        controller.add(StatsService.SERVICE_NAME, new StatsService());

        // -- Stat services
        controller.add(CpuStatService.SERVICE_NAME, new CpuStatService());
        controller.add(RamStatService.SERVICE_NAME, new RamStatService());
        controller.add(TemperatureStatService.SERVICE_NAME, new TemperatureStatService());

        // Attach REST handlers
        RestService.attachControllerRestHandlerServices(controller);

        // Start forever...
        controller.hookAndStartAndWaitForExit();
    }

}
