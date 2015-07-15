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
package au.com.breakpoint.hedron.core.log;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.ResourceScope;
import au.com.breakpoint.hedron.core.ShutdownPriority;
import au.com.breakpoint.hedron.core.concurrent.CallingThreadExecutor;
import au.com.breakpoint.hedron.core.concurrent.Concurrency;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.context.LoggingOperationScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.value.FormattedStringValue;
import au.com.breakpoint.hedron.core.value.HeldValue;
import au.com.breakpoint.hedron.core.value.IValue;

/**
 * These log methods record the thread context id. This allows the tracing and diagnosis
 * of an execution scope, such as a web HTTP POST through the application tiers.
 * Post-mortem analysis can use the thread context id recorded with each log entry,
 * regardless of logging priority (trace, fatal, etc). The thread context id is also
 * recorded with stack traces. With judicious use of logging code and logging threshold
 * configured appropriately, it is possible to determine where and how something went
 * wrong.
 */
public class Logging
{
    // Threadsafe.
    public static void addLogger (final ILogger logger)
    {
        m_loggers.add (logger);
    }

    // Threadsafe.
    public static void clearLoggers ()
    {
        m_loggers.clear ();
    }

    public static void conditionalLogAccumulateString (final String s)
    {
        m_conditionalLogStrings.addLast (s);

        while (m_conditionalLogStrings.size () > MAX_CONDITIONAL_LOG_STRINGS)
        {
            m_conditionalLogStrings.removeFirst ();
        }
    }

    public static void conditionalLogClear ()
    {
        m_conditionalLogStrings.clear ();
    }

    public static void conditionalLogTakeAndLogContents ()
    {
        final String s = conditionalLogTakeContents ();

        logInfoString (s);
    }

    public static String conditionalLogTakeContents ()
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append (String.format ("Accumulated logging %s rows:%n", m_conditionalLogStrings.size ()));

        for (final String s : m_conditionalLogStrings)
        {
            sb.append (String.format ("  %s%n", s));
        }

        conditionalLogClear ();

        return sb.toString ();
    }

    public static synchronized void disableAsyncLogging ()
    {
        // Disable it. Check if async is currently enabled.
        if (m_closeActionDisableAsyncLogging != null)
        {
            // Run the disable async logging action now. Note:
            // m_closeActionDisableAsyncLogging is still stored in
            // HcUtil._m_shutdownTasks and will be called again on shutdown
            // coordination. However, the CloseAction will prevent double execution of
            // the action.
            m_closeActionDisableAsyncLogging.close ();
            m_closeActionDisableAsyncLogging = null;
        }
    }

    public static synchronized ResourceScope<?> enableAsyncLogging ()
    {
        // Check that async not currently enabled.
        if (m_closeActionDisableAsyncLogging == null)
        {
            // Note: the Concurrency.createFixedThreadPool method handles scheduler shutdown.
            m_loggingExecutor = Concurrency.createFixedThreadPool (1, "HcUtil.m_loggingAsyncExecutor", false);

            // On shutdown, restore the logging executor to synchronous so that logging
            // can still be used right through the shutdown process.
            final Runnable task = () ->
            {
                m_loggingExecutor = _SyncExecutor;
                logDebugString ("Logging now returned to synchronous");
                //[debugging only] HcUtil.pause (1000);
            };
            m_closeActionDisableAsyncLogging =
                HcUtil.addShutdownTask (task, ShutdownPriority.AsyncLogging, "async logging");

            logDebugString ("Logging now set to asynchonous");
        }

        return m_closeActionDisableAsyncLogging;
    }

    public static void executeLogging (final Runnable task)
    {
        // Protect against exceptions and keep going so other async tasks are not killed.
        m_loggingExecutor.execute ( () -> ExecutionScopes.executeFaultBarrier (task));
    }

    public static void executeLoggingScoped (final Runnable task)
    {
        // Protect against exceptions and keep going so other async tasks are not killed.
        m_loggingExecutor.execute ( () ->
        {
            try (final LoggingOperationScope ls = new LoggingOperationScope ())
            {
                ExecutionScopes.executeFaultBarrier (task);
            }
        });
    }

    /**
     * Get a copy of the loggers collection. Threadsafe.
     *
     * @return a shallow duplicate of the loggers collection.
     */
    public static final List<ILogger> getLoggers ()
    {
        return new ArrayList<> (m_loggers);
    }

    public static void logDebug (final String format, final Object... parameters)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            // lazy formatting
            logDebugString (new FormattedStringValue (format, parameters));
        }
    }

    public static void logDebugString (final IValue<String> v)
    {
        logDebugString (ThreadContext.getContextId (), v);
    }

    public static void logDebugString (final String s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            logDebugString (new HeldValue<String> (s));
        }
    }

    public static void logDebugString (final String contextId, final IValue<String> v)
    {
        // Do nothing if in logging silence.
        if (!ThreadContext.isWithinLoggingSilence ())
        {
            try (final LoggingOperationScope ls = new LoggingOperationScope ())
            {
                final List<ILogger> loggers = copyLoggers ();// copy loggers since the list may be changed when async action comes in
                executeLoggingScoped ( () ->
                {
                    for (final ILogger l : loggers)
                    {
                        l.logDebug (contextId, v);
                    }
                });
            }
        }
    }

    public static void logError (final String format, final Object... parameters)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            // lazy formatting
            logErrorString (new FormattedStringValue (format, parameters));
        }
    }

    public static void logErrorString (final IValue<String> v)
    {
        logErrorString (ThreadContext.getContextId (), v);
    }

    public static void logErrorString (final String s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            logErrorString (new HeldValue<String> (s));
        }
    }

    public static void logErrorString (final String contextId, final IValue<String> v)
    {
        // Do nothing if in logging silence.
        if (!ThreadContext.isWithinLoggingSilence ())
        {
            try (final LoggingOperationScope ls = new LoggingOperationScope ())
            {
                final List<ILogger> loggers = copyLoggers ();// copy loggers since the list may be changed when async action comes in
                executeLoggingScoped ( () ->
                {
                    for (final ILogger l : loggers)
                    {
                        l.logError (contextId, v);
                    }
                });
            }
        }
    }

    public static void logFatal (final String format, final Object... parameters)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            // lazy formatting
            logFatalString (new FormattedStringValue (format, parameters));
        }
    }

    public static void logFatalString (final IValue<String> v)
    {
        logFatalString (ThreadContext.getContextId (), v);
    }

    public static void logFatalString (final String s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            logFatalString (new HeldValue<String> (s));
        }
    }

    public static void logFatalString (final String contextId, final IValue<String> v)
    {
        // Do nothing if in logging silence.
        if (!ThreadContext.isWithinLoggingSilence ())
        {
            try (final LoggingOperationScope ls = new LoggingOperationScope ())
            {
                final List<ILogger> loggers = copyLoggers ();// copy loggers since the list may be changed when async action comes in
                executeLoggingScoped ( () ->
                {
                    for (final ILogger l : loggers)
                    {
                        l.logFatal (contextId, v);
                    }
                });
            }
        }
    }

    public static void logInfo (final String format, final Object... parameters)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            // lazy formatting
            logInfoString (new FormattedStringValue (format, parameters));
        }
    }

    public static void logInfoString (final IValue<String> v)
    {
        logInfoString (ThreadContext.getContextId (), v);
    }

    public static void logInfoString (final String s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            logInfoString (new HeldValue<String> (s));
        }
    }

    public static void logInfoString (final String contextId, final IValue<String> v)
    {
        // Do nothing if in logging silence.
        if (!ThreadContext.isWithinLoggingSilence ())
        {
            try (final LoggingOperationScope ls = new LoggingOperationScope ())
            {
                final List<ILogger> loggers = copyLoggers ();// copy loggers since the list may be changed when async action comes in
                executeLoggingScoped ( () ->
                {
                    for (final ILogger l : loggers)
                    {
                        l.logInfo (contextId, v);
                    }
                });
            }
        }
    }

    public static void logToConsole (final Level level, final String s)
    {
        final String indented = prefixWithLevel (level, s);

        switch (level)
        {
            case Fatal:
            case Error:
            case Warn:
            {
                System.err.println (indented);
                break;
            }

            default:
            {
                System.out.println (indented);
                break;
            }
        }
    }

    public static void logTrace (final String format, final Object... parameters)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            // lazy formatting
            logTraceString (new FormattedStringValue (format, parameters));
        }
    }

    public static void logTraceString (final IValue<String> v)
    {
        logTraceString (ThreadContext.getContextId (), v);
    }

    public static void logTraceString (final String s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            logTraceString (new HeldValue<String> (s));
        }
    }

    public static void logTraceString (final String contextId, final IValue<String> v)
    {
        // Do nothing if in logging silence.
        if (!ThreadContext.isWithinLoggingSilence ())
        {
            try (final LoggingOperationScope ls = new LoggingOperationScope ())
            {
                final List<ILogger> loggers = copyLoggers ();// copy loggers since the list may be changed when async action comes in
                executeLoggingScoped ( () ->
                {
                    for (final ILogger l : loggers)
                    {
                        l.logTrace (contextId, v);
                    }
                });
            }
        }
    }

    public static void logWarn (final String format, final Object... parameters)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            // lazy formatting
            logWarnString (new FormattedStringValue (format, parameters));
        }
    }

    public static void logWarnString (final IValue<String> v)
    {
        logWarnString (ThreadContext.getContextId (), v);
    }

    public static void logWarnString (final String s)
    {
        try (final LoggingOperationScope ls = new LoggingOperationScope ())
        {
            logWarnString (new HeldValue<String> (s));
        }
    }

    public static void logWarnString (final String contextId, final IValue<String> v)
    {
        // Do nothing if in logging silence.
        if (!ThreadContext.isWithinLoggingSilence ())
        {
            try (final LoggingOperationScope ls = new LoggingOperationScope ())
            {
                final List<ILogger> loggers = copyLoggers ();// copy loggers since the list may be changed when async action comes in
                executeLoggingScoped ( () ->
                {
                    for (final ILogger l : loggers)
                    {
                        l.logWarn (contextId, v);
                    }
                });
            }
        }
    }

    public static Level[] parseLevelString (final String s)
    {
        //if (s == null)
        //{
        //    s = DEFAULT_LOG_LEVEL_CONFIG;
        //}

        final List<Level> l = GenericFactory.newArrayList ();

        s.chars ().forEach (c ->
        {
            switch (c)
            {
                case 't':
                {
                    l.add (Level.Trace);
                    break;
                }

                case 'd':
                {
                    l.add (Level.Debug);
                    break;
                }

                case 'i':
                {
                    l.add (Level.Info);
                    break;
                }

                case 'w':
                {
                    l.add (Level.Warn);
                    break;
                }

                case 'e':
                {
                    l.add (Level.Error);
                    break;
                }

                case 'f':
                {
                    l.add (Level.Fatal);
                    break;
                }

                default:
                {
                    ThreadContext.assertFault (false, "Unsupported log level value [%s]", c);
                    break;
                }
            }
        });

        return l.toArray (new Level[l.size ()]);
    }

    public static String prefixWithLevel (final Level level, final String s)
    {
        final String levelString = level.toString ().toUpperCase ();
        final String message = String.format ("%-5s %s", levelString, s);
        final String indented = message.replaceAll (HcUtil.NewLine, HcUtil.NewLine + "      ");

        return indented;
    }

    // Threadsafe.
    public static void removeLogger (final ILogger logger)
    {
        m_loggers.remove (logger);
    }

    private static List<ILogger> copyLoggers ()
    {
        return GenericFactory.newArrayList (m_loggers);
    }

    //    /** Default to fatal, error, warning, info */
    //    public static final String DEFAULT_LOG_LEVEL_CONFIG = "fewi";

    private static final CallingThreadExecutor _SyncExecutor = new CallingThreadExecutor ();

    /** Not volatile... access is synchronized */
    private static ResourceScope<?> m_closeActionDisableAsyncLogging;

    private static final Deque<String> m_conditionalLogStrings = GenericFactory.newConcurrentLinkedDeque ();

    /**
     * Default to output on the system console with only important messages.
     * CopyOnWriteArrayList is a thread-safe list that is efficient for mostly-iteration
     * usage.
     */
    private static final List<ILogger> m_loggers = new CopyOnWriteArrayList<> ();

    /**
     * Executor used for logging. Defaults to synchronous operation (in calling thread)
     * but can be set to async by calling setAsyncLogging ().
     */
    private static volatile Executor m_loggingExecutor = _SyncExecutor;

    private static final int MAX_CONDITIONAL_LOG_STRINGS = 25;
}
