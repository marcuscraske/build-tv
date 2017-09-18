package com.limpygnome.daemon.buildstatus.model;

import com.limpygnome.daemon.api.LedPattern;

/**
 * Used to hold information about a Jenkins job.
 */
public class JenkinsJob implements Comparable<JenkinsJob>
{
    private String name;
    private LedPattern status;

    public JenkinsJob(String name, LedPattern status)
    {
        this.name = name;
        this.status = status;
    }

    @Override
    public int compareTo(JenkinsJob o)
    {
        return name.compareTo(o.name);
    }

    public String getName()
    {
        return name;
    }

    public LedPattern getStatus()
    {
        return status;
    }

}
