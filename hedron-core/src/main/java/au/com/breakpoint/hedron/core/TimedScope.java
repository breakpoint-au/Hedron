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
package au.com.breakpoint.hedron.core;

import static java.util.Comparator.comparing;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.concurrent.Concurrency;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.FaultException;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.context.ThreadContextException;
import au.com.breakpoint.hedron.core.value.IValue;
import au.com.breakpoint.hedron.core.value.SafeLazyValue;

/**
 * Class providing code block timing support.
 */
public class TimedScope implements Serializable
{
    private TimedScope (final Class<?> usingClass)
    {
        this (usingClass.getSimpleName ());
    }

    private TimedScope (final Class<?> usingClass, final String name)
    {
        this (HcUtil.qualifyName (usingClass, name));
    }

    private TimedScope (final String name)
    {
        m_name = name;
        m_statistics = new AtomicReference<> (TimedScopeStatistics.InitialState);
    }

    private TimedScope (final TimedScope rhs)
    {
        m_name = rhs.m_name;
        m_statistics = new AtomicReference<> (rhs.m_statistics.get ());
    }

    /**
     * Executes and times a service that does not return any data. The execution is not
     * guarded since exceptions are allowed to emit from the service.
     *
     * @param r
     *            Instance of the task to call
     */
    public void execute (final Runnable r)
    {
        execute (r, 0, null);
    }

    /**
     * Executes and times a service that does not return any data. The execution is not
     * guarded since exceptions are allowed to emit from the service. If an alert handler
     * is specified, it is invoked whenever the msecLimit is exceeded.
     *
     * @param r
     *            Instance of the task to call
     */
    public void execute (final Runnable r, final long msecLimit,
        final Consumer<E2<ScopeResult, ScopeOutcome<Void>>> alertHandler)
    {
        execute ( () ->
        {
            r.run ();
            return null;// irrelevant
        } , msecLimit, alertHandler);
    }

    /**
     * Executes a service that returns an item of data. The execution is not guarded since
     * exceptions are allowed to emit from the service.
     *
     * @param <TOutput>
     *            Type of the returned item of data
     * @param task
     *            Instance of the task to call
     * @return Returned item of data
     */
    public <TOutput> TOutput execute (final Supplier<? extends TOutput> task)
    {
        return execute (task, 0, null);
    }

    /**
     * Executes and times a service that returns an item of data. The execution is not
     * guarded since exceptions are allowed to emit from the service. If an alert handler
     * is specified, it is invoked whenever the msecLimit is exceeded.
     *
     * @param <TOutput>
     *            Type of the returned item of data
     * @param task
     *            Instance of the task to call
     * @return Returned item of data
     */
    public <TOutput> TOutput execute (final Supplier<? extends TOutput> task, final long msecLimit,
        final Consumer<E2<ScopeResult, ScopeOutcome<TOutput>>> alertHandler)
    {
        TOutput output = null;

        // Wrap the operation in an execution scope in case the caller isn't using one.
        // This establishes a scoped ThreadContext.getContextId () value. If there is already
        // an outer scope then this just creates a nested scope and doesn't affect the outer
        // context id.
        // NOTE: Java try-with-resources closes the AutoCloseable scope before
        // executing the catch () block, thus losing ThreadContext's context id.
        // Thus any exception logging needs to be done in an inner scope.
        try (final IScope scope = new ExecutionScope (m_name))
        {
            final ScopeOutcome<TOutput> o = new ScopeOutcome<TOutput> (m_name);
            o.setMsecLimit (msecLimit);

            final long nsStart = System.nanoTime ();
            o.setNsStart (nsStart);

            ScheduledFuture<?> timerHandle = null;

            // Supervise the operation.
            try
            {
                if (msecLimit > 0)
                {
                    // Timed supervision required. Kick off the timer.
                    final Runnable command = () ->
                    {
                        // Timeout occurred.
                        final long nsElapsed = System.nanoTime () - nsStart;
                        o.setExpiryDetails (nsElapsed);

                        // Tell the caller about it now.
                        if (alertHandler != null)
                        {
                            alertHandler.accept (E2.of (ScopeResult.TimedOut, o));
                        }
                    };
                    timerHandle = m_executor.get ().schedule (command, msecLimit, TimeUnit.MILLISECONDS);
                }

                // Execute the operation.
                output = task.get ();
                o.setReturnedValue (output);
            }
            catch (final ThreadContextException e)
            {
                // This includes AssertException and FaultException.
                // ThreadContext.assertXxxx methods already log details at time of assertion,
                // but ThreadContext.logException does not re-log them.
                ThreadContext.logException (e);
                o.setCaughtException (e);
                throw e;
            }
            catch (final Throwable e)
            {
                // Handles logging and wraps in FaultException, which is a LogOnceException.
                final FaultException fault = ThreadContext.wrapThrowable (e);
                o.setCaughtException (fault);
                throw fault;
            }
            finally
            {
                final long nsFinish = System.nanoTime ();

                // Stop the timer if it hasn't completed.
                if (timerHandle != null)
                {
                    timerHandle.cancel (false);
                }

                // Gather timing info and update stats.
                final long nsDurationCalc = nsFinish - nsStart;

                // Sun engineer: System.nanoTime() is implemented using the
                // QueryPerformanceCounter / QueryPerformanceFrequency API [...] The default
                // mechanism used by QPC is determined by the Hardware Abstraction
                // layer(HAL) [...] This default changes not only across hardware but also
                // across OS versions. For example Windows XP Service Pack 2 changed things
                // to use the power management timer (PMTimer) rather than the processor
                // timestamp-counter (TSC) due to problems with the TSC not being
                // synchronized on different processors in SMP systems, and due the fact its
                // frequency can vary (and hence its relationship to elapsed time) based on
                // power-management settings
                final long nsDuration = nsDurationCalc < 0 ? 0 : nsDurationCalc;

                o.setNsDuration (nsDuration);
                updateStatistics (o);

                // Generate an alert callback.
                if (alertHandler != null)
                {
                    if (o.didSucceed ())
                    {
                        if (o.didExpire ())
                        {
                            alertHandler.accept (E2.of (ScopeResult.SucceededSlowly, o));
                        }
                    }
                    else
                    {
                        alertHandler.accept (E2.of (ScopeResult.Failed, o));
                    }
                }
            }
        }

        return output;
    }

    public String getName ()
    {
        return m_name;
    }

    public TimedScopeStatistics getStatistics ()
    {
        return m_statistics.get ();
    }

    @Override
    public String toString ()
    {
        final TimedScopeStatistics stats = m_statistics.get ();
        final long durationTotal = stats.getDurationTotal ();
        final long executionsCount = stats.getExecutionsCount ();
        final long successfulExecutionsCount = stats.getSuccessfulExecutionsCount ();
        final long slowExecutionsCount = stats.getSlowExecutionsCount ();
        final long durationMin = executionsCount == 0 ? 0 : stats.getDurationMin ();
        final long durationMax = stats.getDurationMax ();

        final double msecAverage = stats.getAverageMsec ();

        // Work out the average also removing the worst case... allows for
        // timings where the first execution is much slower, eg repeated openings
        // of a pooled database connection.
        final double msecInitialisedAverage = stats.getAverageMsecInit ();

        return String.format ("\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,\"[%s]\"", m_name, HcUtil.nsToMsecDouble (durationTotal),
            executionsCount, successfulExecutionsCount, slowExecutionsCount, HcUtil.nsToMsecDouble (durationMin),
            HcUtil.nsToMsecDouble (durationMax), msecAverage, msecInitialisedAverage,
            stats.getHistogramMsec ().toString ());
    }

    /**
     * Update the stats based on the timing outcome.
     *
     * @param o
     *
     * @return new immutable stats instance.
     */
    private TimedScopeStatistics calculateStatistics (final ScopeOutcome<?> o, final TimedScopeStatistics prevStats)
    {
        long successfulExecutionsCount = prevStats.getSuccessfulExecutionsCount ();
        final boolean ok = o.didSucceed ();
        if (ok)
        {
            ++successfulExecutionsCount;
        }

        long slowExecutionsCount = prevStats.getSlowExecutionsCount ();
        if (o.didExpire ())
        {
            ++slowExecutionsCount;
        }

        final long nsDuration = o.getNsDuration ();
        final long durationTotal = prevStats.getDurationTotal () + nsDuration;
        final long executionsCount = prevStats.getExecutionsCount () + 1;

        long durationMax = prevStats.getDurationMax ();
        if (nsDuration > durationMax)
        {
            durationMax = nsDuration;
        }

        long durationMin = prevStats.getDurationMin ();
        if (nsDuration < durationMin)
        {
            durationMin = nsDuration;
        }

        // Gather duration stats in histogram form.
        final long msecDuration = HcUtil.nsToMsec (nsDuration);
        final LogarithmicHistogram32 histogram = prevStats.getHistogramMsec ().accumulate (msecDuration);

        return new TimedScopeStatistics (durationTotal, executionsCount, successfulExecutionsCount, slowExecutionsCount,
            durationMin, durationMax, histogram);
    }

    private void reset ()
    {
        m_statistics.getAndSet (TimedScopeStatistics.InitialState);
    }

    private void updateStatistics (final ScopeOutcome<?> o)
    {
        // Lock-free implementation (optimistic 'locking' with retry).
        //boolean done;
        //do
        //{
        //    final TimedScopeStatistics prevStats = m_statistics.get ();
        //    final TimedScopeStatistics newStats = calculateStatistics (o, prevStats);
        //
        //    done = m_statistics.compareAndSet (prevStats, newStats);
        //}
        //while (!done);

        // Java 8 encapsulates compareAndSet loop.
        m_statistics.updateAndGet (prevStats -> calculateStatistics (o, prevStats));
    }

    public enum ScopeResult
    {
        Failed, // succeeded but was slow
        SucceededSlowly,
        TimedOut// still running, may eventually succeed
    }

    public static void clearResults ()
    {
        // Concurrent map: no need for synchronized block
        for (final TimedScope ts : m_instances.values ())
        {
            ts.reset ();
        }
    }

    public static String formatResults (final List<TimedScope> results)
    {
        final String header =
            "TimedScope,msecTotal,Executions,Successful,Slow,msecMin,msecMax,msecAverage,msecInitAverage,msecHistogram";
        return HcUtil.formatObjects (header, results);
    }

    public static String getResults (final boolean excludeUnused)
    {
        // Take a copy of the data to avoid locking.
        final List<TimedScope> results = getSortedResults (excludeUnused);

        return formatResults (results);
    }

    public static List<TimedScope> getSortedResults (final boolean excludeUnused)
    {
        final List<TimedScope> results = GenericFactory.newArrayList ();

        // Concurrent map: no need for synchronized block
        for (final TimedScope ts : m_instances.values ())
        {
            if (!excludeUnused || ts.m_statistics.get ().getExecutionsCount () > 0)
            {
                results.add (new TimedScope (ts));// deep copy the data
            }
        }

        results.sort (comparing (TimedScope::getName));

        return results;
    }

    public static TimedScope getTimedScope (final Class<?> usingClass, final String name)
    {
        return getTimedScope (HcUtil.qualifyName (usingClass, name));
    }

    public static TimedScope getTimedScope (final String name)
    {
        return m_instances.computeIfAbsent (name, TimedScope::new);// key == name
    }

    /** Factory method */
    public static TimedScope of (final Class<?> usingClass, final String name)
    {
        return of (HcUtil.qualifyName (usingClass, name));
    }

    /**
     * Execute an Supplier and time it.
     *
     * @param <T>
     *            Type of the returned data from Supplier
     * @param f
     *            the Supplier instance
     * @return A tuple containing the IExecutabel return value plus the duration.
     */
    public static <T> E2<T, Long> timeExecution (final Supplier<T> f)
    {
        final long nsStart = System.nanoTime ();

        // Execute operation.
        final T r = f.get ();

        // Return the execute return value plus the duration.
        final long nsDuration = System.nanoTime () - nsStart;
        return E2.of (r, nsDuration);
    }

    /** Factory method */
    private static TimedScope of (final String name)
    {
        final TimedScope ts = new TimedScope (name);

        // Concurrent map: no need for synchronized block
        m_instances.put (name, ts);

        return ts;
    }

    private final String m_name;

    /** Gather stats in lock-free / immutable manner for max concurrency */
    private final AtomicReference<TimedScopeStatistics> m_statistics;

    private static final int COUNT_TIMER_THREADS = 4;

    /** Timing supervision of execute () */
    private static final IValue<ScheduledExecutorService> m_executor = SafeLazyValue
        .of ( () -> Concurrency.createScheduledThreadPool (COUNT_TIMER_THREADS, "TimedScope.m_executor", false));

    /** Repository of all timed scopes */
    private static final ConcurrentMap<String, TimedScope> m_instances = GenericFactory.newConcurrentHashMap ();

    private static final long serialVersionUID = -7287288369237805994L;

    static
    {
        HcUtil.registerSummaryData (TimedScope::getResults);
    }
}
