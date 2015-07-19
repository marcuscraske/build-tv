package com.limpygnome.daemon.buildtv;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.buildtv.service.JenkinsService;

/**
 * Created by limpygnome on 19/07/15.
 */
public class Program
{
    public static void main(String[] args)
    {
        Controller controller = new Controller();

        // Add services
        controller.add("jenkins-status", new JenkinsService());

        // Start forever...
        controller.hookShutdown();
        controller.start();
        controller.waitForExit();
    }
}
