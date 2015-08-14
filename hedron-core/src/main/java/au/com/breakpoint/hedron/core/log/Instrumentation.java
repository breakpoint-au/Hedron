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
package au.com.breakpoint.hedron.core.log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import au.com.breakpoint.hedron.core.Counter;
import au.com.breakpoint.hedron.core.CounterRange;
import au.com.breakpoint.hedron.core.CounterThroughput;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.ICloseable;
import au.com.breakpoint.hedron.core.MaxCounter;
import au.com.breakpoint.hedron.core.NullFormatter;
import au.com.breakpoint.hedron.core.ScopeOutcome;
import au.com.breakpoint.hedron.core.ShutdownPriority;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.TimedScope.ScopeResult;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.args4j.HcUtilArgs4j;
import au.com.breakpoint.hedron.core.concurrent.CallingThreadExecutor;
import au.com.breakpoint.hedron.core.concurrent.Concurrency;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.remote.MockInstrumentionRemoteLogger;
import au.com.breakpoint.hedron.core.value.IValue;
import au.com.breakpoint.hedron.core.value.SafeLazyValue;

/**
 * Class providing profiling / timing support and logging to multiple instrumentation
 * handlers / servers.
 */
public class Instrumentation
{
    public static void addInstrumentationListener (final IInstrumentionListener l)
    {
        m_instrumentationListeners.add (l);
    }

    public static void configure (final String logFolder, final String levelsConfigLocal, final int logDaysToKeep,
        final boolean shouldLogConsole, final IInstrumentionListenerLogger remoteInstrumentationLogger)
    {
        // Add local logging based on levelsConfigLocal levels.
        if (HcUtil.safeGetLength (levelsConfigLocal) > 0)
        {
            // Slave the formatted output to the console.
            final List<IStringLogger> slaves =
                shouldLogConsole ? GenericFactory.newArrayList (new ConsoleStringLogger ()) : null;
            final AbstractLogger logger = new FolderPerDayLogger (logFolder, levelsConfigLocal, logDaysToKeep, slaves);

            // Link into logging.
            Logging.addLogger (logger);

            // Handle instrumentation events using a simple instrumentationListener
            // that writes its events to the local logger.
            addInstrumentationListener (new InstrumentionStringLogger (logger));
        }

        // Handle instrumentation events using a provided remote instrumentationListener.
        // It is also a logger, so hook it into the logging infrastructure so
        // that it can perform remote logging also.
        if (remoteInstrumentationLogger != null)
        {
            m_instrumentationListeners.add (remoteInstrumentationLogger);
            Logging.addLogger (remoteInstrumentationLogger);
        }
    }

    public static void execute (final String name, final long msecLimit, final Runnable r,
        final String limitPropertyName)
    {
        final TimedScope scope = TimedScope.getTimedScope (name);
        execute (scope, r, msecLimit, limitPropertyName);
    }

    public static <T> T execute (final String name, final long msecLimit, final Supplier<T> task,
        final String limitPropertyName)
    {
        final TimedScope scope = TimedScope.getTimedScope (name);
        return execute (scope, task, msecLimit, limitPropertyName);
    }

    public static void execute (final TimedScope scope, final Runnable task, final long msecLimit,
        final String limitPropertyName)
    {
        final Supplier<Void> wrapper = () ->
        {
            task.run ();
            return null;
        };
        execute (scope, wrapper, msecLimit, limitPropertyName);
    }

    public static <T> T execute (final TimedScope scope, final Supplier<T> task, final long msecLimit,
        final String limitPropertyName)
    {
        final Consumer<E2<ScopeResult, ScopeOutcome<T>>> alertHandler = o ->
        {
            final ScopeOutcome<T> so = o.getE1 ();

            // Callback on significant event while executing.
            switch (o.getE0 ())
            {
                case Failed:
                {
                    alertFailed (so, limitPropertyName);
                    break;
                }

                case SucceededSlowly:
                {
                    alertSlow (so, limitPropertyName);
                    break;
                }

                case TimedOut:
                {
                    alertTimeout (so, limitPropertyName);
                    break;
                }

                default:
                {
                    ThreadContext.assertFault (false, "Unsupported value [%s]", o.getE0 ());
                    break;
                }
            }
        };
        return scope.execute (task, msecLimit, alertHandler);
    }

    /**
     * Implements the execution scope pattern for a program main (). This implements a top
     * level fault barrier, with a nested ExecutionScope.
     */
    public static void executeProgram (final String appname, final String buildString, final Runnable service,
        final ILogConfiguration logConfiguration)
    {
        // TODO _ review JSW alternatives ... eg 64 bit capable ... docker etc
        // This scope handles HcUtil.onShutdown () at the end to shutdown background threads.
        ExecutionScopes.executeProgram ( () ->
        {
            // Add code to execute on system exit.
            HcUtil.addShutdownTask ( () ->
            {
                // Send final execution info as datagram as we don't want to hold up the shutdown.
                publishExecutionSummary ();

                Logging.logInfo ("Shutting down %s", appname);
                shutdown ();
            } , ShutdownPriority.ExecutionSummaryWriting, "execution summary writing");// higher priority than threadpool, less than IProcessor shutdowns

            HcUtil.setApplicationName (appname, buildString);

            if (logConfiguration != null)
            {
                final List<E2<String, String>> symbols = HcUtil.getStandardSubstitutions ();
                final String localLogFolder = HcUtil.substituteSymbols (logConfiguration.getLogFolder (), symbols);

                // TODO _ instantiate proper remote IInstrumentationLogger
                final IInstrumentionListenerLogger remoteInstrumentatorLogger =
                    new MockInstrumentionRemoteLogger (new NullFormatter (), logConfiguration.getLevelsConfigRemote ());

                configure (localLogFolder, logConfiguration.getLevelsConfigLocal (),
                    logConfiguration.getLogDaysToKeep (), logConfiguration.shouldLogConsole (),
                    remoteInstrumentatorLogger);

                Logging.logInfo (
                    "Configured Instrumentation for %s; local logging folder [%s] [%s]; remote logging [%s]", appname,
                    localLogFolder, logConfiguration.getLevelsConfigLocal (),
                    logConfiguration.getLevelsConfigRemote ());
            }

            schedulePublishExecutionSummary ();

            // Make logging happen asychronously for the duration of this try scope.
            try (final ICloseable loggingScope = Logging.enableAsyncLogging ())
            {
                // Run for the lifetime of the program.
                service.run ();

                // A service running under Java Service Wrapper  doesn't return here since
                // the JSW calls System.exit (), which calls HcUtil.coordinateShutdown (). A
                // standalone application does return here, and then the finally clause of
                // ExecutionScopedServiceHandler.executeProgram () calls
                // HcUtil.coordinateShutdown (). NB: the eventual Java exit also calls
                // HcUtil.coordinateShutdown () but there is nothing left for it to do since the
                // stored CloseAction entities tolerate multiple calls to close ().
            }
        });
    }

    public static boolean getPolicyAsyncInstrumentation ()
    {
        return m_policyAsyncInstrumentation;
    }

    /** Test program for shutdown */
    public static void main (final String... args)
    {
        class Options implements ILogConfiguration
        {
            @Override
            public String getLevelsConfigLocal ()
            {
                return "fewid";
            }

            @Override
            public String getLevelsConfigRemote ()
            {
                return "fewi";
            }

            @Override
            public int getLogDaysToKeep ()
            {
                return 10;
            }

            @Override
            public String getLogFolder ()
            {
                return "c:/temp/_testlog";
            }

            @Override
            public boolean shouldLogConsole ()
            {
                return true;
            }
        }

        final Options options = new Options ();

        // Check args & prepare usage string (in thrown AssertException).
        HcUtilArgs4j.getProgramOptions (args, options);
        executeProgram ("TestApp", "SomeApp 10.0#13 (27/02/2014 14:02) by Breakpoint Pty Limited", () ->
        {
            ThreadContext.assertWarning (false, "Warning: %s", "xxx");
            HcUtil.pause (2000);
        } , options);
    }

    public static void publishExecutionSummary ()
    {
        final boolean excludeUnused = true;

        final List<TimedScope> listTimedScope = TimedScope.getSortedResults (excludeUnused);
        final List<CounterThroughput> listCounterThroughput = CounterThroughput.getSortedResults (excludeUnused);
        final List<Counter> listCounter = Counter.getSortedResults (excludeUnused);
        final List<CounterRange> listCounterRange = CounterRange.getSortedResults (excludeUnused);
        final List<MaxCounter> listMaxCounter = MaxCounter.getSortedResults (excludeUnused);

        final String contextId = ThreadContext.getContextId ();

        m_instrumentationListeners.forEach (inst -> inst.onExecutionSummary (contextId, listTimedScope,
            listCounterThroughput, listCounter, listCounterRange, listMaxCounter));
    }

    public static void scheduleShutdown (final int runForMsec)
    {
        final Timer timer = new Timer ();
        final TimerTask task = new TimerTask ()
        {
            @Override
            public void run ()
            {
                ExecutionScopes.executeFaultBarrier ( () ->
                {
                    timer.cancel ();
                    HcUtil.systemExit (0);// shutdown hook will terminate processors
                });
            }
        };
        timer.schedule (task, runForMsec);
    }

    public static void setPolicyAsyncInstrumentation (final boolean policyAsyncInstrumentation)
    {
        m_policyAsyncInstrumentation = policyAsyncInstrumentation;
    }

    public static void shutdown ()
    {
        // Instrumentation is shutting down, so prevent remote logging attempts.
        m_instrumentationListeners.forEach (inst ->
        {
            inst.setActive (false);
        });
        m_instrumentationListeners.clear ();
    }

    private static void alertFailed (final ScopeOutcome<?> o, final String limitPropertyName)
    {
        // Logging.logInfoString (o.getCaughtException ().getMessage ());

        // This is running in the context of execution thread, not the monitoring thread pool.
        final String contextId = o.getContextId ();

        // Execute this asynchronously.
        executeAsyncProtected ( () ->
        {
            // Perform scope-protected functionality.
            final Throwable caughtException = o.getCaughtException ();

            // If FaultException, don't generate another exception message since it is
            // already logged in ThreadContext.throwFault ().
            String messageText = "";
            if (caughtException != null)
            {
                final boolean hasBeenLogged = ThreadContext.hasBeenLogged (caughtException);

                messageText = hasBeenLogged ? "Refer to associated exception logged for context " + contextId
                    : HcUtil.getExceptionDetails (caughtException);
            }

            final long msecDuration = HcUtil.nsToMsec (o.getNsDuration ());
            final long msecLimit = o.getMsecLimit ();
            final String operationName = o.getName ();
            final String s = messageText;
            m_instrumentationListeners.forEach (inst -> inst.onOperationFailed (contextId, s, operationName,
                msecDuration, msecLimit, limitPropertyName));
        });
    }

    private static void alertSlow (final ScopeOutcome<?> o, final String limitPropertyName)
    {
        // This is running in the context of execution thread, not the monitoring thread pool.
        final String contextId = o.getContextId ();

        //[never fails] ThreadContext.assertFault (ThreadContext.getContextId ().equals (contextId), "Logic error! [%s] [%s]", ThreadContext.getContextId (), contextId);

        // Execute this asynchronously.
        executeAsyncProtected ( () ->
        {
            final long msecDuration = HcUtil.nsToMsec (o.getNsDuration ());
            final long msecLimit = o.getMsecLimit ();
            final String operationName = o.getName ();
            m_instrumentationListeners.forEach (
                inst -> inst.onOperationSlow (contextId, operationName, msecDuration, msecLimit, limitPropertyName));
        });
    }

    private static void alertTimeout (final ScopeOutcome<?> o, final String limitPropertyName)
    {
        // This is running in the context of the monitoring thread pool, not the execution thread.
        final String contextId = o.getContextId ();// use original thread's context

        // Execute this asynchronously.
        executeAsyncProtected ( () ->
        {
            final long msecDuration = HcUtil.nsToMsec (o.getNsDuration ());
            final long msecLimit = o.getMsecLimit ();
            final String operationName = o.getName ();
            m_instrumentationListeners.forEach (inst -> inst.onOperationTimedOut (contextId, operationName,
                msecDuration, msecLimit, limitPropertyName));
        });
    }

    private static void executeAsyncProtected (final Runnable task)
    {
        ExecutionScopes.executeTaskProtected (m_threadPool.get (), task);
    }

    private static void schedulePublishExecutionSummary ()
    {
        // Publish stats periodically to the instrumentation handlers.
        HcUtil.schedulePeriodically (Instrumentation::publishExecutionSummary, EXECUTION_SUMMMARY_SEND_PERIOD_MSEC);
    }

    private static final int EXECUTION_SUMMMARY_SEND_PERIOD_MSEC = 15 * 60 * 1000;

    private static final List<IInstrumentionListener> m_instrumentationListeners = GenericFactory.newArrayList ();

    private static boolean m_policyAsyncInstrumentation = true;

    /** Thread for async instrumentation actions (or synchronous for testing etc) */
    private static final IValue<Executor> m_threadPool = SafeLazyValue.of ( () -> m_policyAsyncInstrumentation
        ? Concurrency.createFixedThreadPool (1, "Instrumentation.m_threadPool", false, true).get ()
        : new CallingThreadExecutor ());
}
