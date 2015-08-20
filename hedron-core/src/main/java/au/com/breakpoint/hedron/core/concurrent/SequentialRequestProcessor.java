//                       __________________________________
//                ______|      Copyright 2008-2015         |______
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Processor that handles requests sequentially, taking them from a specified request
 * queue passed via IProcessorInit.initialise ().
 *
 * @param <T>
 *            Type of request handed
 */
public class SequentialRequestProcessor<T> implements IProcessorInit<BlockingQueue<T>>
{
    public SequentialRequestProcessor (final String name, final Consumer<? super T> task, final long shutdownClearMsec)
    {
        this (name, shutdownClearMsec);
        setTask (task);
    }

    public SequentialRequestProcessor (final String name, final long shutdownClearMsec)
    {
        m_name = name;
        m_shutdownClearMsec = shutdownClearMsec;
    }

    @Override
    public void awaitShutdownComplete ()
    {
        try
        {
            m_shutdownLatch.await (15, TimeUnit.SECONDS);
        }
        catch (final InterruptedException e)
        {
            // Shutting down... don't care.
        }

        Logging.logInfo ("%s shutdown complete %s", this, System.nanoTime ());
    }

    @Override
    public void initialise (final BlockingQueue<T> context)
    {
        m_requestQueue = context;
    }

    @Override
    public void processUntilShutdown ()
    {
        ThreadContext.assertFaultNotNull (m_task);
        Logging.logInfo ("%s starting execution", this);

        // Run the logging task, which is the consumer of logging events from the queue.
        for (m_nsRunUntil = -1; shouldContinue ();)
        {
            try
            {
                // Retrieves and removes the head of this queue, waiting up to the
                // specified wait time if necessary for an element to become available.
                final T req = m_requestQueue.poll (POLL_TIMEOUT_MSEC, TimeUnit.MILLISECONDS);
                if (req != null)
                {
                    // Request received before timeout.
                    // Wrap in a nested scope for context id tracking.
                    try (final IScope scope = new ExecutionScope ("SequentialRequestProcessor"))
                    {
                        m_task.accept (req);
                    }
                }
            }
            catch (final InterruptedException e)
            {
                // shutting down... operation abandoned
            }
        }

        m_shutdownLatch.countDown ();
    }

    public void setTask (final Consumer<? super T> task)
    {
        m_task = task;
    }

    @Override
    public void signalShutdown ()
    {
        final long now = System.nanoTime ();
        m_nsRunUntil = now + HcUtil.msecToNs (m_shutdownClearMsec);

        Logging.logInfo ("%s initiating shutdown (%s->%s)", this, now, m_nsRunUntil);
    }

    @Override
    public String toString ()
    {
        return String.format ("SequentialRequestProcessor [%s]", m_name);
    }

    private boolean shouldContinue ()
    {
        // Keep running until the queue is empty or a time limit is reached.
        return m_nsRunUntil == -1L || HcUtil.nsIsBefore (System.nanoTime (),
            m_nsRunUntil) && m_requestQueue.size () > 0;
    }

    private final String m_name;

    /**
     * Used to coordinate shutdown. Keep runnings until the queue is empty or a time limit
     * is reached.
     */
    private volatile long m_nsRunUntil;

    private volatile BlockingQueue<T> m_requestQueue;

    private final long m_shutdownClearMsec;

    private final CountDownLatch m_shutdownLatch = new CountDownLatch (1);

    private volatile Consumer<? super T> m_task;

    private static final int POLL_TIMEOUT_MSEC = 1_000;
}
