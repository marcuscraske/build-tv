package com.limpygnome.daemon.buildstatus.service;

import com.limpygnome.daemon.api.Controller;
import com.limpygnome.daemon.api.Service;
import com.limpygnome.daemon.buildstatus.jenkins.JenkinsStatusThread;
import com.limpygnome.daemon.buildstatus.model.JenkinsHostUpdateResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service responsible for controlling thread to perform periodic Jenkins polling.
 */
public class JenkinsService implements Service
{
    private static final Logger LOG = LogManager.getLogger(JenkinsService.class);

    public static final String SERVICE_NAME = "jenkins-status";

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

    /**
     * @return The latest result from polling Jenkins
     */
    public JenkinsHostUpdateResult getLatestResult()
    {
        return jenkinsStatusThread.getLatestResult();
    }

}
