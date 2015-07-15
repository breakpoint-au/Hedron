package au.com.breakpoint.hedron.core.concurrent;

import java.util.concurrent.CountDownLatch;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Block until shutdown.
 */
public class NullProcessor implements IProcessor
{
    public NullProcessor (final String name)
    {
        m_name = name;
    }

    @Override
    public void awaitShutdownComplete ()
    {
        // Shuts down completely in signalShutdown. Nothing to wait for.
        Logging.logInfo ("%s shutdown complete %s", this, System.nanoTime ());
    }

    @Override
    public void processUntilShutdown ()
    {
        Logging.logInfo ("%s starting execution", this);

        try
        {
            m_shutdownLatch.await ();
        }
        catch (final InterruptedException e)
        {
            // Shutting down... don't care.
        }
    }

    @Override
    public void signalShutdown ()
    {
        Logging.logInfo ("%s initiating shutdown", this);

        m_shutdownLatch.countDown ();
    }

    @Override
    public String toString ()
    {
        return String.format ("NullProcessor [%s]", m_name);
    }

    private final String m_name;

    /** Used to coordinate shutdown */
    private final CountDownLatch m_shutdownLatch = new CountDownLatch (1);
}
