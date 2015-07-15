package au.com.breakpoint.hedron.core.concurrent;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.xml.ws.Endpoint;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Processor that holds and coordinates a set of web service endpoints. The IProcessorInit
 * parameterised type is an collection of [web service, endoint url] tuples.
 */
public class WebServicesProcessor implements IProcessorInit<List<E2<Object, String>>>
{
    public WebServicesProcessor (final String name)
    {
        m_name = name;
    }

    @Override
    public void awaitShutdownComplete ()
    {
        // Shuts down completely in signalShutdown. Nothing to wait for.
        Logging.logInfo ("%s shutdown complete", this);
    }

    @Override
    public void initialise (final List<E2<Object, String>> context)
    {
        for (final E2<Object, String> ws : context)
        {
            final Object service = ws.getE0 ();
            final String urlWebService = ws.getE1 ();

            // Endpoint.publish does not block.
            final Endpoint publishedService = Endpoint.publish (urlWebService, service);
            m_endpoints.add (publishedService);

            Logging.logInfo ("%s endpoint [%s] WSDL [%s?WSDL]", this, urlWebService, urlWebService);
        }
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

        for (final Endpoint endpoint : m_endpoints)
        {
            endpoint.stop ();
        }

        m_shutdownLatch.countDown ();
    }

    @Override
    public String toString ()
    {
        return String.format ("WebServicesProcessor [%s]", m_name);
    }

    private final List<Endpoint> m_endpoints = GenericFactory.newArrayList ();

    private final String m_name;

    /** Used to coordinate shutdown */
    private final CountDownLatch m_shutdownLatch = new CountDownLatch (1);
}
