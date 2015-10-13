package com.limpygnome.daemon.util;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by limpygnome on 20/07/15.
 */
public class EnvironmentUtil
{
    private static final Logger LOG = LogManager.getLogger(EnvironmentUtil.class);

    private static final boolean DEV_ENVIRONMENT;

    static
    {
        DEV_ENVIRONMENT = new File("pom.xml").exists();

        if (DEV_ENVIRONMENT)
        {
            LOG.warn("Development environment detected, flag set to true...");
        }
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
