package com.limpygnome.daemon.system;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.system.service.*;
import com.limpygnome.daemon.service.RestService;
import com.limpygnome.daemon.system.service.stat.CpuStatService;
import com.limpygnome.daemon.system.service.stat.RamStatService;
import com.limpygnome.daemon.system.service.stat.TemperatureStatService;

/**
 * Entry point into the system daemon.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller("system-daemon");

        // Add services
        controller.add(EnvironmentService.SERVICE_NAME, new EnvironmentService());
        controller.add("screen", new ScreenService());
        controller.add("power", new PowerService());
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