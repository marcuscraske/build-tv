package com.limpygnome.daemon.util;

import java.io.File;

/**
 * Created by limpygnome on 20/07/15.
 */
public class EnvironmentUtil
{
    private static final boolean DEV_ENVIRONMENT;

    static
    {
        DEV_ENVIRONMENT = new File("pom.xml").exists();
    }

    /**
     * Useful for hardware stubbing for local dev testing.
     *
     * @return
     */
    public static boolean isDevEnvironment()
    {
        return DEV_ENVIRONMENT;
    }

}
