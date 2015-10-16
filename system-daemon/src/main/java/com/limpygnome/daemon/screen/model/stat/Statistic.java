package com.limpygnome.daemon.screen.model.stat;

/**
 * Created by limpygnome on 16/10/15.
 */
public class Statistic
{
    private String text;
    private float min;
    private float max;
    private float value;

    public Statistic(String text, float min, float max, float value)
    {
        this.text = text;
        this.min = min;
        this.max = max;
        this.value = value;
    }

    public String getText()
    {
        return text;
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
}
