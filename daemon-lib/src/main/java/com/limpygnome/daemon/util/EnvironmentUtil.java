package com.limpygnome.daemon.util;

import java.io.File;

/**
 * Created by limpygnome on 20/07/15.
 */
public class EnvironmentUtil
{

    /**
     * Useful for hardware stubbing for local dev testing.
     *
     * @return
     */
    public static boolean isDevEnvironment()
    {
        return new File("pom.xml").exists();
    }

}
