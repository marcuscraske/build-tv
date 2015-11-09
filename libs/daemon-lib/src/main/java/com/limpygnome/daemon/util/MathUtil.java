package com.limpygnome.daemon.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by limpygnome on 20/10/15.
 */
public class MathUtil
{

    public static float round(float value, int places)
    {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(places, RoundingMode.HALF_UP).floatValue();
    }

}
