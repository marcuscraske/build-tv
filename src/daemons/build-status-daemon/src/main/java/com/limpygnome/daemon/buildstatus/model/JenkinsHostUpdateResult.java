package com.limpygnome.daemon.buildstatus.model;

import com.limpygnome.daemon.api.LedPattern;

import java.util.*;

/**
 * Used to hold the latest result from polling Jenkins.
 */
public class JenkinsHostUpdateResult
{
    private LedPattern ledPattern;
    private List<String> affectedJobs;
    private Set<JenkinsJob> jobs;

    public JenkinsHostUpdateResult()
    {
        this(LedPattern.BUILD_UNKNOWN);
    }

    public JenkinsHostUpdateResult(LedPattern ledPattern)
    {
        this.ledPattern = ledPattern;
        this.affectedJobs = new LinkedList<>();
        this.jobs = new TreeSet<>();
    }

    /**
     * @return The current LED pattern.
     */
    public LedPattern getLedPattern()
    {
        return ledPattern;
    }

    public void setLedPattern(LedPattern ledPattern)
    {
        this.ledPattern = ledPattern;
    }

    /**
     * @return List of affected jobs for the current LED pattern (for failures etc).
     */
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

    public void addJob(JenkinsJob jobName)
    {
        this.jobs.add(jobName);
    }

    public void mergeJobs(JenkinsHostUpdateResult result)
    {
        this.jobs.addAll(result.jobs);
    }

    /**
     * @return The jobs found whilst polling
     */
    public Set<JenkinsJob> getJobs()
    {
        return jobs;
    }

}
