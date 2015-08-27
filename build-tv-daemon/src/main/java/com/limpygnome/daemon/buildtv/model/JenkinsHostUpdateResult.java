package com.limpygnome.daemon.buildtv.model;

import com.limpygnome.daemon.buildtv.led.pattern.LedPattern;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by limpygnome on 27/08/15.
 */
public class JenkinsHostUpdateResult
{
    private LedPattern ledPattern;
    private List<String> affectedJobs;

    public JenkinsHostUpdateResult()
    {
        this(LedPattern.BUILD_UNKNOWN);
    }

    public JenkinsHostUpdateResult(LedPattern ledPattern)
    {
        this.ledPattern = ledPattern;
        this.affectedJobs = new LinkedList<>();
    }

    public LedPattern getLedPattern()
    {
        return ledPattern;
    }

    public void setLedPattern(LedPattern ledPattern)
    {
        this.ledPattern = ledPattern;
    }

    public List<String> getAffectedJobs()
    {
        return affectedJobs;
    }

    public void addAffectedJob(String jobName)
    {
        this.affectedJobs.add(jobName);
    }

    public void mergeAffectedJobs(JenkinsHostUpdateResult result)
    {
        this.affectedJobs.addAll(result.affectedJobs);
    }
}
