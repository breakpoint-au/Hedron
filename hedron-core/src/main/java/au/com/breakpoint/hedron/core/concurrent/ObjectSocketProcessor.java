//                       __________________________________
//                ______|         Copyright 2008           |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.core.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.MaxCounter;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * ObjectSocketProcessor is a process that listens on a socket, accepts incoming requests.
 * Datagrams message are queued immediately to the m_socketRequestHandlingExecutor thread
 * pool, which then deserialises the incoming message back into the protocol object and
 * despatches it for handling.
 */
public class ObjectSocketProcessor<TRequest, TResponse> implements IProcessor
{
    public ObjectSocketProcessor (final Class<?> c, final int port, final int threads)
    {
        this (c.getSimpleName (), port, threads);
    }

    public ObjectSocketProcessor (final String name, final int port, final int threads)
    {
        m_name = name;
        m_port = port;

        m_timedScopeHandleRequest = TimedScope.getTimedScope (ObjectSocketProcessor.class, name);
        m_countConcurrentSocketsMax = MaxCounter.of (ObjectSocketProcessor.class, "concurrentSockets." + name);

        // Creates a bounded queue to prevent overload.
        // NB: Don't use Concurrency.createFixedThreadPool, since it handles scheduler shutdown,
        // whereas we want to handle shutdown within this processor.
        final String identifier = toString ();
        m_socketRequestHandlingExecutor = Concurrency.createFixedThreadPool (threads, identifier, false, false).get ();
    }

    @Override
    public void awaitShutdownComplete ()
    {
        final String identifier = toString ();
        HcUtil.awaitShutdownExecutorService (m_socketRequestHandlingExecutor, identifier, MSEC_SHUTDOWN,
            TimeUnit.MILLISECONDS);
    }

    @Override
    public void processUntilShutdown ()
    {
        Logging.logInfo ("%s starting execution on port %s", this, m_port);

        try (final ServerSocket serverSocket = new ServerSocket (m_port))
        {
            // Make call to accept () for this ServerSocket will block for only this
            // amount of time. If the timeout expires, a SocketTimeoutException is raised, though
            // the ServerSocket is still valid.
            serverSocket.setSoTimeout (POLL_TIMEOUT_MSEC);

            while (!m_shutdownRequested)
            {
                try
                {
                    final Socket socketConnection = serverSocket.accept ();

                    // Handle the message concurrently and get back to listening for more
                    // messages.
                    final Runnable task = () ->
                    {
                        // Wrap in a nested scope for context id tracking.
                        try (final IScope scope = new ExecutionScope ("ObjectSocketProcessor"))
                        {
                            handleRequest (socketConnection);
                        }
                    };

                    if (!m_shutdownRequested)
                    {
                        m_socketRequestHandlingExecutor.execute (task);
                    }
                }
                catch (final SocketTimeoutException e)
                {
                    // No message received, keep waiting for m_shutdownRequested.
                    //System.out.print ('.');
                }
            }
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    public void setCommsEventCallback (final Consumer<E2<InetAddress, Boolean>> commsEventCallback)
    {
        m_commsEventCallback = commsEventCallback;
    }

    public void setMessageHandler (final Function<E2<InetAddress, TRequest>, TResponse> messageHandler)
    {
        m_messageHandler = messageHandler;
    }

    @Override
    public void signalShutdown ()
    {
        // Initiates an orderly shutdown in which previously submitted tasks are
        // executed, but no new tasks will be accepted. Invocation has no
        // additional effect if already shut down.
        m_shutdownRequested = true;

        final String identifier = toString ();
        HcUtil.startShutdownExecutorService (m_socketRequestHandlingExecutor, identifier);
    }

    @Override
    public String toString ()
    {
        return String.format ("ObjectSocketProcessor [%s]", m_name);
    }

    private void adviseBadCommsThrow (final Socket socketConnection, final Exception e)
    {
        final InetAddress inetAddress = socketConnection.getInetAddress ();
        if (m_commsEventCallback != null)
        {
            m_commsEventCallback.accept (E2.of (inetAddress, false));
        }

        ThreadContext.assertError (false, "ObjectSocketProcessor client comms timeout %s %s", inetAddress,
            e.toString ());
    }

    private void adviseGoodComms (final Socket socketConnection)
    {
        if (m_commsEventCallback != null)
        {
            final InetAddress inetAddress = socketConnection.getInetAddress ();
            m_commsEventCallback.accept (E2.of (inetAddress, true));
        }
    }

    private void handleRequest (final Socket socketConnection)
    {
        final Runnable task = () ->
        {
            m_countConcurrentSocketsMax.increment ();
            try
            {
                socketConnection.setSoTimeout (MSEC_TIMEOUT_DATA);
            }
            catch (final SocketException e)
            {
                ThreadContext.throwFault (e);
            }

            //final InetAddress address = socketConnection.getInetAddress ();
            //final String clientIpAddress = address.getHostAddress ();
            //final String clientHost = address.getHostName ();

            try (final ObjectInputStream ois = new ObjectInputStream (socketConnection.getInputStream ()))
            {
                @SuppressWarnings ("unchecked")
                final TRequest r = (TRequest) ois.readObject ();

                final InetAddress inetAddress = socketConnection.getInetAddress ();

                // Pass to the configured function for handling.
                final TResponse response = m_messageHandler.apply (E2.of (inetAddress, r));

                if (response != null)
                {
                    try (final ObjectOutputStream oos = new ObjectOutputStream (socketConnection.getOutputStream ()))
                    {
                        oos.writeObject (response);
                        oos.flush ();
                    }
                }

                // Comms is good including outgoing reply.
                adviseGoodComms (socketConnection);
            }
            catch (final IOException e)
            {
                adviseBadCommsThrow (socketConnection, e);
            }
            catch (final ClassNotFoundException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
            finally
            {
                // Socket passed to worker thread, hence no try-with-resources.
                try
                {
                    socketConnection.close ();
                }
                catch (final Throwable e)
                {
                    // Fault barrier.
                    ThreadContext.logException (e);
                }

                m_countConcurrentSocketsMax.decrement ();
            }
        };
        m_timedScopeHandleRequest.execute (task);
    }

    /** The handler for notifications */
    private volatile Consumer<E2<InetAddress, Boolean>> m_commsEventCallback;

    private final MaxCounter m_countConcurrentSocketsMax;

    /**
     * The handler for incoming objects, yields outgoing object. E2<InetAddress, TRequest>
     * is (IP address, request).
     */
    private volatile Function<E2<InetAddress, TRequest>, TResponse> m_messageHandler;

    private final String m_name;

    private final int m_port;

    /** Used to coordinate shutdown */
    private volatile boolean m_shutdownRequested;

    private final ExecutorService m_socketRequestHandlingExecutor;

    private final TimedScope m_timedScopeHandleRequest;

    protected static final int MSEC_TIMEOUT_DATA = 60_000;

    private static final long MSEC_SHUTDOWN = 5_000;

    private static final int POLL_TIMEOUT_MSEC = 1_000;
}
