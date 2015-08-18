package com.limpygnome.daemon.buildtv.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.buildtv.jenkins.JenkinsStatusThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

/**
 * Created by limpygnome on 19/07/15.
 */
public class JenkinsService implements Service
{
    private static final Logger LOG = LogManager.getLogger(JenkinsService.class);

    private JenkinsStatusThread jenkinsStatusThread;

    public JenkinsService()
    {
        this.jenkinsStatusThread = null;
    }

    @Override
    public synchronized void start(Controller controller)
    {
        // Start thread
        jenkinsStatusThread = new JenkinsStatusThread(controller);
        jenkinsStatusThread.start();
    }

    @Override
    public synchronized void stop(Controller controller)
    {
        if (jenkinsStatusThread != null)
        {
            // Stop thread
            jenkinsStatusThread.kill();
            jenkinsStatusThread = null;
        }
    }

}
