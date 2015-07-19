package com.limpygnome.ws281x.daemon.led.imp;

import com.limpygnome.ws281x.daemon.led.api.Pattern;
import com.limpygnome.ws281x.daemon.service.imp.LedService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by limpygnome on 18/07/15.
 *
 * TODO: move into generic thread, since Java thread object is blurgh...
 */
public class LedRenderThread extends Thread
{
    private static final Logger LOG = LogManager.getLogger(LedRenderThread.class);

    // The current pattern to render
    private Pattern currentPattern;

    // Indicates if to exit
    private boolean exit;

    // The service to which this thread belongs
    private LedService ledService;
    private LedController ledController;

    public LedRenderThread(LedService ledService, Pattern currentPattern)
    {
        this.exit = false;
        this.ledService = ledService;
        this.ledController = ledService.getLedController();
        this.currentPattern = currentPattern;
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName("LED Rendering");

        LOG.debug("Starting render - pattern: {}", currentPattern.getClass().getName());

        try
        {
            currentPattern.render(this, ledController);
        }
        catch (Exception e)
        {
            LOG.error("Exception during rendering", e);
        }

        LOG.debug("Finished render - pattern: {}", currentPattern.getClass().getName());
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

}
