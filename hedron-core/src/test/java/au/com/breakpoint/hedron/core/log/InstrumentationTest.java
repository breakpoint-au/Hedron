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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import au.com.breakpoint.hedron.core.Counter;
import au.com.breakpoint.hedron.core.CounterRange;
import au.com.breakpoint.hedron.core.CounterThroughput;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.MaxCounter;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.context.ExecutionScope;
import au.com.breakpoint.hedron.core.context.IScope;
import au.com.breakpoint.hedron.core.context.ThreadContext;

public class InstrumentationTest
{
    @Test
    public void testInstrumentation ()
    {
        try (IScope scope = new ExecutionScope ())
        {
            Instrumentation.setPolicyAsyncInstrumentation (false);// execute actions synchronously

            final E2<DummyLogger, DummyRemoteInstrumentationLogger> loggers = setupLoggers ("fewi");
            final DummyLogger localLogger = loggers.getE0 ();
            final DummyRemoteInstrumentationLogger remoteLogger = loggers.getE1 ();

            if (true)
            {
                Instrumentation.publishExecutionSummary ();
                checkInstrumentationListener (remoteLogger, 1, 0, 0, 0);
                checkLogger (localLogger, 0, 0, 0, 1, 0, 0);
                checkLogger (remoteLogger, 0, 0, 0, 0, 0, 0);
            }

            if (true)
            {
                final String name = "timelyOperation";
                Instrumentation.execute (name, 200, () ->
                {
                    HcUtil.pause (10);
                } , name);

                Instrumentation.publishExecutionSummary ();
                checkInstrumentationListener (remoteLogger, 2, 0, 0, 0);
                checkLogger (localLogger, 0, 0, 0, 2, 0, 0);
                checkLogger (remoteLogger, 0, 0, 0, 0, 0, 0);
            }

            if (true)
            {
                final String name = "slowOperation";
                Instrumentation.execute (name, 10, () ->
                {
                    HcUtil.pause (100);
                } , name);

                Instrumentation.publishExecutionSummary ();
                checkInstrumentationListener (remoteLogger, 3, 1, 1, 0);
                checkLogger (localLogger, 0, 0, 2, 3, 0, 0);
                checkLogger (remoteLogger, 0, 0, 0, 0, 0, 0);
            }

            if (true)
            {
                try
                {
                    final String name = "failedTimelyOperation";
                    Instrumentation.execute (name, 200, () ->
                    {
                        HcUtil.pause (10);
                        ThreadContext.assertFaultNotNull (null);
                    } , name);
                }
                catch (final Exception e)
                {
                }

                Instrumentation.publishExecutionSummary ();
                checkInstrumentationListener (remoteLogger, 4, 1, 1, 1);
                checkLogger (localLogger, 2, 0, 2, 4, 0, 0);
                checkLogger (remoteLogger, 1, 0, 0, 0, 0, 0);
            }

            if (true)
            {
                try
                {
                    final String name = "failedTimelyOperationException";
                    Instrumentation.execute (name, 200, () ->
                    {
                        HcUtil.pause (10);
                        throw new RuntimeException ("Ooops");
                    } , name);
                }
                catch (final Exception e)
                {
                }

                Instrumentation.publishExecutionSummary ();
                checkInstrumentationListener (remoteLogger, 5, 1, 1, 2);
                checkLogger (localLogger, 4, 0, 2, 5, 0, 0);
                checkLogger (remoteLogger, 2, 0, 0, 0, 0, 0);
            }
        }
    }

    @Test
    public void testLogging ()
    {
        try (IScope scope = new ExecutionScope ())
        {
            final E2<DummyLogger, DummyRemoteInstrumentationLogger> loggers = setupLoggers ("fewidt");
            final DummyLogger localLogger = loggers.getE0 ();
            final DummyRemoteInstrumentationLogger remoteLogger = loggers.getE1 ();

            final String fatal = "fatal test string";
            final String error = "error test string";
            final String warn = "warn test string";
            final String info = "info test string";
            final String debug = "debug test string";
            final String trace = "trace test string";

            Logging.logInfoString (info);
            checkLogger (localLogger, null, null, null, info, null, null);
            checkLogger (remoteLogger, null, null, null, info, null, null);

            Logging.logFatalString (fatal);
            checkLogger (localLogger, fatal, null, null, info, null, null);
            checkLogger (remoteLogger, fatal, null, null, info, null, null);

            Logging.logErrorString (error);
            checkLogger (localLogger, fatal, error, null, info, null, null);
            checkLogger (remoteLogger, fatal, error, null, info, null, null);

            Logging.logWarnString (warn);
            checkLogger (localLogger, fatal, error, warn, info, null, null);
            checkLogger (remoteLogger, fatal, error, warn, info, null, null);

            Logging.logDebugString (debug);
            checkLogger (localLogger, fatal, error, warn, info, debug, null);
            checkLogger (remoteLogger, fatal, error, warn, info, null, null);

            Logging.logTraceString (trace);
            checkLogger (localLogger, fatal, error, warn, info, debug, trace);
            checkLogger (remoteLogger, fatal, error, warn, info, null, null);
        }
    }

    private void checkInstrumentationListener (final DummyRemoteInstrumentationLogger remoteLogger, final int summary,
        final int timedOut, final int slow, final int failed)
    {
        assertEquals (summary, remoteLogger.m_valueOnExecutionSummary.size ());
        assertEquals (timedOut, remoteLogger.m_valueOnOperationTimedOut.size ());
        assertEquals (slow, remoteLogger.m_valueOnOperationSlow.size ());
        assertEquals (failed, remoteLogger.m_valueOnOperationFailed.size ());
    }

    private E2<DummyLogger, DummyRemoteInstrumentationLogger> setupLoggers (final String levelsConfig)
    {
        // Red of Instrumentation.configure ()
        Logging.clearLoggers ();

        final DummyRemoteInstrumentationLogger remoteLogger =
            new DummyRemoteInstrumentationLogger ("fewi", getTempPath ("log-remote.txt"));

        final List<ConsoleStringLogger> slaves = OUTPUT_TO_CONSOLE ? Arrays.asList (new ConsoleStringLogger ()) : null;
        final DummyLogger localLogger = new DummyLogger (levelsConfig, slaves, getTempPath ("log-local.txt"));
        Logging.addLogger (localLogger);

        Instrumentation.addInstrumentationListener (new InstrumentionStringLogger (localLogger));

        Instrumentation.addInstrumentationListener (remoteLogger);
        Logging.addLogger (remoteLogger);

        return E2.of (localLogger, remoteLogger);
    }

    public static class DummyRemoteInstrumentationLogger extends DummyLogger implements IInstrumentionListenerLogger
    {
        public DummyRemoteInstrumentationLogger (final String levelsConfig, final String logToFilename)
        {
            super (levelsConfig, null, logToFilename);
        }

        /** IInstrumentationLogger */
        @Override
        public void onExecutionSummary (final String contextId, final List<TimedScope> listTimedScope,
            final List<CounterThroughput> listCounterThroughput, final List<Counter> listCounter,
            final List<CounterRange> listCounterRange, final List<MaxCounter> listMaxCounter)
        {
            m_valueOnExecutionSummary.add (contextId);
            writeToFile (Level.Info, HcUtil.deepToString ("onExecutionSummary", contextId, listTimedScope,
                listCounterThroughput, listCounter, listCounterRange, listMaxCounter));
        }

        /** IInstrumentationLogger */
        @Override
        public void onOperationFailed (final String contextId, final String messageText, final String operationName,
            final long msecDuration, final long msecLimit, final String limitPropertyName)
        {
            m_valueOnOperationFailed.add (messageText);
            writeToFile (Level.Fatal, HcUtil.deepToString ("onOperationFailed", messageText, operationName,
                msecDuration, msecLimit, limitPropertyName, contextId));
        }

        /** IInstrumentationLogger */
        @Override
        public void onOperationSlow (final String contextId, final String operationName, final long msecDuration,
            final long msecLimit, final String limitPropertyName)
        {
            m_valueOnOperationSlow.add (operationName);
            writeToFile (Level.Warn, HcUtil.deepToString ("onOperationSlow", operationName, msecDuration, msecLimit,
                limitPropertyName, contextId));
        }

        /** IInstrumentationLogger */
        @Override
        public void onOperationTimedOut (final String contextId, final String operationName, final long msecDuration,
            final long msecLimit, final String limitPropertyName)
        {
            m_valueOnOperationTimedOut.add (operationName);
            writeToFile (Level.Warn, HcUtil.deepToString ("onOperationTimedOut", operationName, msecDuration, msecLimit,
                limitPropertyName, contextId));
        }

        List<String> m_valueOnExecutionSummary = GenericFactory.newArrayList ();

        List<String> m_valueOnOperationFailed = GenericFactory.newArrayList ();

        List<String> m_valueOnOperationSlow = GenericFactory.newArrayList ();

        List<String> m_valueOnOperationTimedOut = GenericFactory.newArrayList ();
    }

    //    private void checkLoggers (final int fatal, final int error, final int warn, final int info, final int debug,
    //        final int trace)
    //    {
    //        checkLogger (localLogger, fatal, error, warn, info, debug, trace);
    //        checkLogger (remoteLogger, fatal, error, warn, info, debug, trace);
    //    }

    private static void checkArrayEquals (final String expectedString, final List<String> l)
    {
        if (expectedString == null)
        {
            assertEquals (0, l.size ());
        }
        else
        {
            assertArrayEquals (new String[]
            {
                    expectedString
            }, l.toArray (new String[l.size ()]));
        }
    }

    private static void checkLogger (final DummyLogger logger, final int fatal, final int error, final int warn,
        final int info, final int debug, final int trace)
    {
        checkSize (fatal, logger.m_valueLogFatal);
        checkSize (error, logger.m_valueLogError);
        checkSize (warn, logger.m_valueLogWarn);
        checkSize (info, logger.m_valueLogInfo);
        checkSize (debug, logger.m_valueLogDebug);
        checkSize (trace, logger.m_valueLogTrace);
    }

    private static void checkLogger (final DummyLogger logger, final String fatal, final String error,
        final String warn, final String info, final String debug, final String trace)
    {
        checkArrayEquals (fatal, logger.m_valueLogFatal);
        checkArrayEquals (error, logger.m_valueLogError);
        checkArrayEquals (warn, logger.m_valueLogWarn);
        checkArrayEquals (info, logger.m_valueLogInfo);
        checkArrayEquals (debug, logger.m_valueLogDebug);
        checkArrayEquals (trace, logger.m_valueLogTrace);
    }

    private static void checkSize (final int expected, final List<String> l)
    {
        if (expected != l.size ())
        {
            System.err.printf ("Problem: %s%n", l);
        }
        assertEquals (expected, l.size ());
    }

    private static String getTempPath (final String filename)
    {
        return HcUtil.formFilepath (HcUtil.getTempDirectoryName (), filename);
    }

    private static final boolean OUTPUT_TO_CONSOLE = false;
}
