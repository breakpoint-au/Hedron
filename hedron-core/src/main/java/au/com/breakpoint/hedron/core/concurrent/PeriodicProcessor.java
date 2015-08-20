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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Periodically performs an action.
 */
public class PeriodicProcessor implements IProcessor
{
    public PeriodicProcessor (final Class<?> c, final int periodMsec)
    {
        this (c.getSimpleName (), periodMsec);
    }

    public PeriodicProcessor (final Class<?> c, final Runnable task, final int periodMsec)
    {
        this (c.getSimpleName (), task, periodMsec);
    }

    public PeriodicProcessor (final String name, final int periodMsec)
    {
        this (name, periodMsec, 60_000);// default 1 minute shutdown timeout
    }

    public PeriodicProcessor (final String name, final int periodMsec, final int shutdownTimeoutMsec)
    {
        m_name = name;
        m_periodMsec = periodMsec;
        m_baseTimeMsec = System.currentTimeMillis ();
        m_shutdownTimeoutMsec = shutdownTimeoutMsec;

        // Get this from TimedScope static collection in case the PeriodicProcessor
        // is recreated after failure by RetryProcessor.
        m_timedScopeExecute = TimedScope.getTimedScope (PeriodicProcessor.class, name);
    }

    public PeriodicProcessor (final String name, final Runnable task, final int periodMsec)
    {
        this (name, periodMsec);

        setTask (task);
    }

    /**
     * This is invoked on process shutdown after all IProcessor instances have received a
     * signalShutdown call.
     */
    @Override
    public void awaitShutdownComplete ()
    {
        if (!m_running)
        {
            Logging.logDebug ("%s shutdown on idle", this);
        }
        else
        {
            Logging.logInfo ("%s awaiting shutdown", this);
            try
            {
                final boolean completed = m_shutdownLatch.await (m_shutdownTimeoutMsec, TimeUnit.MILLISECONDS);
                if (!completed)
                {
                    Logging.logWarn ("%s shutdown before periodic task completed", this);
                }
            }
            catch (final InterruptedException e)
            {
                // Shutting down... don't care.
            }
        }

        Logging.logInfo ("%s shutdown complete", this);
    }

    public OverrunPolicy getOverrunPolicy ()
    {
        return m_overrunPolicy;
    }

    public boolean isActive ()
    {
        return !m_shutdownRequested;
    }

    /**
     * This implementation executes the task functionality in the context of the thread
     * that calls processUntilShutdown ().
     */
    @Override
    public void processUntilShutdown ()
    {
        ThreadContext.assertFaultNotNull (m_task);
        m_running = true;

        Logging.logInfo ("%s starting execution at %s period", this, HcUtil.formatMilliseconds (m_periodMsec));

        boolean shouldWaitForNextPeriod = true;
        do
        {
            final long msecToWait = calculateMsecToWait ();
            //System.out.printf ("msecToWait %s  ", msecToWait);

            // The variable shouldWaitForNext is used to for overrun handling.
            if (shouldWaitForNextPeriod && msecToWait > 0)
            {
                try
                {
                    // Wait on the latch until either the timeout occurs or
                    // the latch is counted down indicating shutdown initiated.
                    final boolean completed = m_latchShutdownInitiated.await (msecToWait, TimeUnit.MILLISECONDS);
                    if (completed)
                    {
                        Logging.logDebug ("%s shutdown before timeout", this);
                    }
                }
                catch (final InterruptedException e)
                {
                    // Propagate exception as unchecked fault up to the fault barrier.
                    ThreadContext.throwFault (e);
                }
            }

            // If shutting down, don't execute the task.
            if (!m_shutdownRequested)
            {
                final long nsStart = System.nanoTime ();

                // Don't need to wrap in a nested scope for context id tracking
                // since TimedScope.execute () already does this.
                m_timedScopeExecute.execute (m_task::run);

                // Calculate duration for overrun detection.
                final long nsDuration = System.nanoTime () - nsStart;

                // If have overrun and OverrunPolicy.RunImmediately, don't wait.
                shouldWaitForNextPeriod =
                    HcUtil.nsToMsec (nsDuration) < m_periodMsec || m_overrunPolicy == OverrunPolicy.WaitForNextPeriod;
            }
        }
        while (!m_shutdownRequested);

        // Indicate processor thread shut down.
        m_shutdownLatch.countDown ();

        m_running = false;
    }

    public void setOverrunPolicy (final OverrunPolicy overrunPolicy)
    {
        m_overrunPolicy = overrunPolicy;
    }

    public void setTask (final Runnable task)
    {
        m_task = task;
    }

    /**
     * This is invoked on process shutdown.
     */
    @Override
    public void signalShutdown ()
    {
        m_shutdownRequested = true;

        // Interrupt if waiting for next periodic task.
        m_latchShutdownInitiated.countDown ();
    }

    @Override
    public String toString ()
    {
        return String.format ("PeriodicProcessor [%s]", m_name);
    }

    private long calculateMsecToWait ()
    {
        final long nowMsec = System.currentTimeMillis ();

        // Force to wait one period before executing on startup.
        final long fromMsec = nowMsec == m_baseTimeMsec ? nowMsec + 1 : nowMsec;

        final long msecToWait = calculateMsecToWait (fromMsec, m_periodMsec, m_baseTimeMsec);

        // Put in a defensive check in case there is some time-based weirdness
        // such as when clocks change or a logic bug, etc.
        return msecToWait < 0 || msecToWait > m_periodMsec ? m_periodMsec : msecToWait;
    }

    public enum OverrunPolicy
    {
        RunImmediately, WaitForNextPeriod
    }

    /** Exposed for unit testing */
    static long calculateMsecToWait (final long nowMsec, final int periodMsec, final long baseTimeMsec)
    {
        // Time since startup.
        final long msecSinceBase = nowMsec - baseTimeMsec;

        // Work out time of next complete period, ie next time to take action etc.
        final long intervalCount = (msecSinceBase + periodMsec - 1) / periodMsec;
        final long nextMsec = baseTimeMsec + intervalCount * periodMsec;

        // How long to wait until time of next complete period.
        final long msecToWait = nextMsec - nowMsec;

        return msecToWait;
    }

    private final long m_baseTimeMsec;

    /** Used to coordinate execution until shutdown */
    private final CountDownLatch m_latchShutdownInitiated = new CountDownLatch (1);

    private final String m_name;

    private volatile OverrunPolicy m_overrunPolicy = OverrunPolicy.WaitForNextPeriod;

    private final int m_periodMsec;

    private volatile boolean m_running;

    /** Used to coordinate shutdown */
    private final CountDownLatch m_shutdownLatch = new CountDownLatch (1);

    /** Used to coordinate shutdown */
    private volatile boolean m_shutdownRequested;

    private final int m_shutdownTimeoutMsec;;

    /** The work periodically performed */
    private volatile Runnable m_task;

    private final TimedScope m_timedScopeExecute;
}
