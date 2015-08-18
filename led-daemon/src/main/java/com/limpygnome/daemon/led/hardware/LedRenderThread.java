package com.limpygnome.daemon.led.hardware;

import com.limpygnome.daemon.common.ExtendedThread;
import com.limpygnome.daemon.led.service.LedService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by limpygnome on 18/07/15.
 *
 * TODO: move into generic thread, since Java thread object is blurgh...
 */
public class LedRenderThread extends ExtendedThread
{
    private static final Logger LOG = LogManager.getLogger(LedRenderThread.class);

    // The current pattern to render
    private Pattern currentPattern;

    // The service to which this thread belongs
    private LedService ledService;
    private LedController ledController;

    public LedRenderThread(LedService ledService, Pattern currentPattern)
    {
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
            if (ledController != null)
            {
                currentPattern.render(this, ledController);
            }
            else
            {
                LOG.warn("Unable to render LED pattern, no LED controller - {}", currentPattern);
            }
        }
        catch (Exception e)
        {
            LOG.error("Exception during rendering", e);
        }

        LOG.debug("Finished render - pattern: {}", currentPattern.getClass().getName());
    }

}
