package com.limpygnome.daemon.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by limpygnome on 19/07/15.
 */
public abstract class ExtendedThread extends Thread
{
    private static final Logger LOG = LogManager.getLogger(ExtendedThread.class);

    // Indicates if to exit
    private boolean exit;

    public ExtendedThread()
    {
        this.exit = false;
    }

    public boolean isExit()
    {
        return exit;
    }

    public synchronized void kill()
    {
        this.exit = true;

        if (Thread.currentThread() != this)
        {
            // In case thread is sleeping...
            interrupt();

            // Wait for thread to end...
            try
            {
                join();
            }
            catch (InterruptedException e)
            {
                LOG.error("Failed to kill thread", e);
            }
        }
    }

    @Override
    public abstract void run();
}
