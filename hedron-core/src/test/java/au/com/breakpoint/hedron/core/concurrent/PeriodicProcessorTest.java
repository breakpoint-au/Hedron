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

import static org.junit.Assert.assertEquals;
import java.util.Timer;
import java.util.TimerTask;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.concurrent.IProcessor;
import au.com.breakpoint.hedron.core.concurrent.PeriodicProcessor;
import au.com.breakpoint.hedron.core.concurrent.RetryProcessor;
import au.com.breakpoint.hedron.core.context.FaultException;
import au.com.breakpoint.hedron.core.context.LoggingSilenceScope;

public class PeriodicProcessorTest
{
    @Test
    public void testCalculateMsecToWait ()
    {
        final int periodMsec = 10;
        final int baseTimeMsec = 63;
        doTestCalculateMsecToWait (67, periodMsec, baseTimeMsec, 73);
        doTestCalculateMsecToWait (103, periodMsec, baseTimeMsec, 103);
        doTestCalculateMsecToWait (104, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (105, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (106, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (107, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (108, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (109, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (110, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (111, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (112, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (113, periodMsec, baseTimeMsec, 113);
        doTestCalculateMsecToWait (114, periodMsec, baseTimeMsec, 123);
        doTestCalculateMsecToWait (115, periodMsec, baseTimeMsec, 123);
        doTestCalculateMsecToWait (116, periodMsec, baseTimeMsec, 123);
    }

    @Test
    public void testProcessUntilShutdown ()
    {
        final Runnable task = new Runnable ()
        {
            @Override
            public void run ()
            {
                final long beforeMsec = System.currentTimeMillis () - m_startTimeMsec;
                HcUtil.pause (MSEC_DURATION);
                System.out.printf ("elapsed %s -> %s%n", beforeMsec, System.currentTimeMillis () - m_startTimeMsec);
            }

            private volatile long m_startTimeMsec = System.currentTimeMillis ();
        };
        runProcessor (new PeriodicProcessor ("testProcessUntilShutdown", task, MSEC_PERIOD), MSEC_RUN_DURATION);
    }

    @Test (expected = FaultException.class)
    public void testProcessUntilShutdownException ()
    {
        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            final Runnable task = getExceptionalRunnable (0);
            final PeriodicProcessor rp = new TestingPeriodicProcessor (task, MSEC_PERIOD);
            runProcessor (rp, MSEC_RUN_DURATION_EXCEPTION);
        }
    }

    public void testProcessUntilShutdownExceptions ()
    {
        // Prevent logging of expected FaultException
        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            final Runnable task = getExceptionalRunnable (2);
            final PeriodicProcessor p = new PeriodicProcessor ("testProcessUntilShutdownExceptions", task, MSEC_PERIOD);
            runProcessor (p, MSEC_RUN_DURATION);
        }
    }

    @Test
    public void testProcessUntilShutdownExceptionsRetry ()
    {
        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            final Runnable task = getExceptionalRunnable (2);
            final RetryProcessor rp = new RetryProcessor ( () -> new TestingPeriodicProcessor (task, MSEC_PERIOD), 1);
            runProcessor (rp, MSEC_RUN_DURATION);
        }
    }

    private void doTestCalculateMsecToWait (final int nowMsec, final int periodMsec, final int baseTimeMsec,
        final int expectedNextMsec)
    {
        assertEquals (expectedNextMsec - nowMsec,
            PeriodicProcessor.calculateMsecToWait (nowMsec, periodMsec, baseTimeMsec));
    }

    private Runnable getExceptionalRunnable (final int exceptionAfter)
    {
        return new Runnable ()
        {
            @Override
            public void run ()
            {
                if (++m_count >= exceptionAfter)
                {
                    m_count = 0;
                    try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
                    {
                        throw new FaultException ("Deliberate", false);
                    }
                }
            }

            private int m_count;
        };
    }

    private void runProcessor (final IProcessor rp, final int durationMsec)
    {
        final Timer timer = new Timer ();
        final TimerTask timeoutAction = new TimerTask ()
        {
            @Override
            public void run ()
            {
                rp.signalShutdown ();
                rp.awaitShutdownComplete ();
            }
        };
        timer.schedule (timeoutAction, durationMsec);

        rp.processUntilShutdown ();
    }

    private static class TestingPeriodicProcessor extends PeriodicProcessor
    {
        public TestingPeriodicProcessor (final Runnable task, final int periodMsec)
        {
            super (TestingPeriodicProcessor.class, task, periodMsec);
        }
    }

    private static final int MSEC_DURATION = 5;

    private static final int MSEC_PERIOD = 50;

    private static final int MSEC_RUN_DURATION = 200;

    private static final int MSEC_RUN_DURATION_EXCEPTION = 200;
}
