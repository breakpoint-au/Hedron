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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.HcUtilFile;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * ObjectDatagramProcessor is a process that listens on a socket, accepts incoming
 * datagrams. Datagrams message are queued immediately to the
 * m_socketRequestHandlingExecutor thread pool, which then deserialises the incoming
 * message and despatches for handling.
 */
public class ObjectDatagramProcessor<TRequest> implements IProcessor
{
    public ObjectDatagramProcessor (final Class<?> c, final int port, final int threads)
    {
        this (c.getSimpleName (), port, threads);
    }

    public ObjectDatagramProcessor (final String name, final int port, final int threads)
    {
        m_name = name;
        m_port = port;

        m_timedScopeHandleRequest = TimedScope.getTimedScope (ObjectDatagramProcessor.class, name);

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

        try (final DatagramSocket socket = new DatagramSocket (m_port))
        {
            // Make call to accept () for this ServerSocket will block for only this
            // amount of time. If the timeout expires, a SocketTimeoutException is raised, though
            // the ServerSocket is still valid.
            socket.setSoTimeout (POLL_TIMEOUT_MSEC);

            while (!m_shutdownRequested)
            {
                final byte[] buffer = new byte[DATAGRAM_BUFFER_BYTE];
                final DatagramPacket packet = new DatagramPacket (buffer, buffer.length);

                // Receives a datagram packet from this socket. When this method returns,
                // the <code>DatagramPacket</code>'s buffer is filled with the data
                // received. The datagram packet also contains the sender's IP address,
                // and the port number on the sender's machine.
                try
                {
                    socket.receive (packet);

                    // Queue the datagram packet for handling message concurrently and get back
                    // to listening for more socket messages.
                    if (!m_shutdownRequested)
                    {
                        m_socketRequestHandlingExecutor.execute ( () ->
                        {
                            // Wrap in a nested scope for context id tracking.
                            try (final IScope scope = new ExecutionScope ("ObjectDatagramProcessor"))
                            {
                                handleRequest (packet);
                            }
                        });
                    }
                }
                catch (final SocketTimeoutException e)
                {
                    // No message received, keep waiting for m_shutdownRequested.
                    // System.out.print ('.');
                }
            }
        }
        catch (final SocketException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
    }

    public void setMessageHandler (final Consumer<E2<String, TRequest>> messageHandler)
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
        return String.format ("ObjectDatagramProcessor [%s]", m_name);
    }

    /**
     * Deferred handling of datagram packet.
     *
     * @param packet
     *            The received datagram
     */
    private void handleRequest (final DatagramPacket packet)
    {
        m_timedScopeHandleRequest.execute ( () ->
        {
            final InetAddress address = packet.getAddress ();
            final String clientIpAddress = address.getHostAddress ();

            // Can't use ObjectInputStream since the request comes in as a datagram packet.
            final byte[] data = packet.getData ();

            final TRequest r = HcUtilFile.deserialiseBytesAsObject (data);

            m_messageHandler.accept (E2.of (clientIpAddress, r));
            return null;
        });
    }

    /**
     * Handler for incoming datagrams. Takes IP address + deserialised request object. It
     * is called concurrently as soon as the socket receives the datagram.
     */
    private volatile Consumer<E2<String, TRequest>> m_messageHandler;

    private final String m_name;

    private final int m_port;

    /** Used to coordinate shutdown */
    private volatile boolean m_shutdownRequested;

    /**
     * Thread pool of handlers for logging requests coming in on the socket. Each handler
     * parses the incoming data, formats the logging data, and puts it into
     * m_loggingWorkQueue for a single logging thread to pick up and append to the file.
     */
    private final ExecutorService m_socketRequestHandlingExecutor;

    private final TimedScope m_timedScopeHandleRequest;

    private static final int DATAGRAM_BUFFER_BYTE = 65536;

    private static final long MSEC_SHUTDOWN = 5000;

    private static final int POLL_TIMEOUT_MSEC = 1000;
}
