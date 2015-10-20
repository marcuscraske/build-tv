package com.limpygnome.daemon.system.model.stat;

import java.io.Serializable;

/**
 * Created by limpygnome on 16/10/15.
 */
public class Statistic implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String key;
    private float min;
    private float max;
    private float value;
    private String unit;

    public Statistic(String key, float min, float max, float value, String unit)
    {
        this.key = key;
        this.min = min;
        this.max = max;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public float getMin()
    {
        return min;
    }

    public float getMax()
    {
        return max;
    }

    public float getValue()
    {
        return value;
    }

    public String getUnit()
    {
        return unit;
    }

}
