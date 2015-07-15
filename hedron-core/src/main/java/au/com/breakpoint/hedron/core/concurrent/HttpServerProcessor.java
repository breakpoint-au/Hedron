package au.com.breakpoint.hedron.core.concurrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

public class HttpServerProcessor implements IProcessor
{
    public HttpServerProcessor (final Class<?> c, final BiFunction<String, String, E2<Integer, String>> task,
        final HttpConfiguration config)
    {
        this (c.getSimpleName (), task, config);
    }

    public HttpServerProcessor (final Class<?> c, final HttpConfiguration config)
    {
        this (c.getSimpleName (), config);
    }

    public HttpServerProcessor (final String name, final BiFunction<String, String, E2<Integer, String>> task,
        final HttpConfiguration config)
    {
        this (name, config);
        setTask (task);
    }

    public HttpServerProcessor (final String name, final HttpConfiguration config)
    {
        m_name = name;
        m_config = config;

        // Get this from TimedScope static collection in case the HttpServerProcessor
        // is recreated after failure by RetryProcessor.
        m_timedScopeExecute = TimedScope.getTimedScope (HttpServerProcessor.class, name);
    }

    @Override
    public void awaitShutdownComplete ()
    {
        // Let m_loggingTask run time limited keep until all logged.
        Logging.logInfo ("%s shutdown complete %s", this, System.nanoTime ());
    }

    @Override
    public void processUntilShutdown ()
    {
        Logging.logInfo ("%s starting execution at http://localhost:%s%s", this, m_config.m_port,
            m_config.m_contextPath);
        m_server = startHttpServer (m_config);

        // The HTTP server executes concurrently. Block here until shutdown.
        try
        {
            m_shutdownLatch.await ();
        }
        catch (final InterruptedException e)
        {
            // Shutting down... don't care.
        }

        System.out.println ();
    }

    public void setTask (final BiFunction<String, String, E2<Integer, String>> task)
    {
        m_task = task;
    }

    @Override
    public void signalShutdown ()
    {
        m_server.stop (0);
        m_shutdownLatch.countDown ();

        Logging.logInfo ("%s initiating shutdown", this);
    }

    @Override
    public String toString ()
    {
        return String.format ("HttpServerProcessor [%s]", m_name);
    }

    private HttpServer startHttpServer (final HttpConfiguration config)
    {
        HttpServer server = null;
        try
        {
            server = HttpServer.create (new InetSocketAddress (config.m_port), 0);
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        final HttpHandler httpHandler = he ->
        {
            // Wrap in a nested scope for context id tracking.
            try (final IScope scope = new ExecutionScope ("HttpServerProcessor"))
            {
                final String method = he.getRequestMethod ();

                String data = null;
                try (final InputStreamReader isr = new InputStreamReader (he.getRequestBody (), "utf-8");
                    final BufferedReader br = new BufferedReader (isr))
                {
                    final StringBuilder sb = new StringBuilder ();

                    String line;
                    boolean first = true;
                    while ((line = br.readLine ()) != null)
                    {
                        if (first)
                        {
                            first = false;
                        }
                        else
                        {
                            sb.append ("\r\n");
                        }

                        sb.append (line);
                    }

                    data = sb.toString ();

                }
                catch (final IOException e)
                {
                    ThreadContext.throwFault (e);
                }

                final String finalData = data;
                final E2<Integer, String> respDetails = m_timedScopeExecute.execute ( () ->
                {
                    final E2<Integer, String> resp = m_task != null ? m_task.apply (method, finalData) : UNIMPLEMENTED;

                    return resp != null ? resp : UNIMPLEMENTED;
                });

                final int code = respDetails.getE0 ();

                final String response = respDetails.getE1 ();
                try
                {
                    he.sendResponseHeaders (code, response.length ());
                    try (final OutputStream os = he.getResponseBody ())
                    {
                        os.write (response.getBytes ());
                    }
                }
                catch (final IOException e)
                {
                    ThreadContext.throwFault (e);
                }
            }
        };
        server.createContext (config.m_contextPath, httpHandler);

        server.setExecutor (null);// creates a default executor
        server.start ();

        return server;
    }

    public static class HttpConfiguration
    {
        public HttpConfiguration (final int port, final String contextPath)
        {
            m_port = port;
            m_contextPath = contextPath;
        }

        public String getContextPath ()
        {
            return m_contextPath;
        }

        public int getPort ()
        {
            return m_port;
        }

        final String m_contextPath;

        final int m_port;
    }

    /** A standalone test program only */
    public static void main (final String[] args)
    {
        ExecutionScopes.executeProgram ( () ->
        {
            // http://localhost:8000/test
            final int port = 8000;
            final String contextPath = "/test";
            final int seconds = 10;

            final BiFunction<String, String, E2<Integer, String>> task = (method, data) ->
            {
                final String response = "This is the response to " + method;
                return E2.of (200, response);
            };

            final HttpServerProcessor p =
                new HttpServerProcessor ("Testing", task, new HttpConfiguration (port, contextPath));

            HcUtil.scheduleOnce ( () ->
            {
                p.signalShutdown ();
                p.awaitShutdownComplete ();
            } , seconds * 1000);

            System.out.println ("http://localhost:8000/test");

            // Blocks.
            p.processUntilShutdown ();
        });
    }

    private final HttpConfiguration m_config;

    private final String m_name;

    private volatile HttpServer m_server;

    /** Used to coordinate shutdown */
    private final CountDownLatch m_shutdownLatch = new CountDownLatch (1);

    private volatile BiFunction<String, String, E2<Integer, String>> m_task;

    private final TimedScope m_timedScopeExecute;

    private static final E2<Integer, String> UNIMPLEMENTED = E2.of (405, "Service not implemented");
}
