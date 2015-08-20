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

import java.util.Timer;
import java.util.TimerTask;
import org.junit.Test;
import au.com.breakpoint.hedron.core.concurrent.IProcessor;
import au.com.breakpoint.hedron.core.concurrent.MultiProcessor;
import au.com.breakpoint.hedron.core.concurrent.Processors;
import au.com.breakpoint.hedron.core.concurrent.RetryProcessor;
import au.com.breakpoint.hedron.core.concurrent.mock.ProcessorMock;
import au.com.breakpoint.hedron.core.context.AssertException;
import au.com.breakpoint.hedron.core.context.LoggingSilenceScope;

public class MultiProcessorTest
{
    //    @Test
    //    public void testMemoryRelease ()
    //    {
    //        for (;;)
    //        {
    //            final A a = new A ();
    //            final B b = new B ();
    //            a.m_b = b;
    //            b.m_a = a;
    //        }
    //    }

    @Test (expected = AssertException.class)
    public void testProcessException0 ()
    {
        performExceptionTest (0, 3);// exception from auxiliary thread
    }

    @Test (expected = AssertException.class)
    public void testProcessException1 ()
    {
        performExceptionTest (1, 3);// exception from auxiliary thread
    }

    @Test (expected = AssertException.class)
    public void testProcessException2 ()
    {
        performExceptionTest (2, 3);// exception from *this* thread
    }

    @Test
    public void testProcessUntilShutdown ()
    {
        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            final IProcessor rp = new ProcessorMock ("-a", false, MSEC_RUN_DURATION / 10);
            final IProcessor rpe = new RetryProcessor ( () -> new ProcessorMock ("-b", true, MSEC_RUN_DURATION / 10),
                MSEC_RUN_DURATION / 15);

            final MultiProcessor mp = new MultiProcessor (rp, rpe);
            runProcessor (mp, MSEC_RUN_DURATION);
        }
    }

    /**
     * If this runs for 10 seconds instead of 50 msec, MultiProcessor.processUntilShutdown
     * () is not working properly by catching the exceptions as they occur.
     */
    private void performExceptionTest (final int exceptionalProcessorIndex, final int count)
    {
        final int msecDuration = 10 * 1000;
        final int msecUntilException = 50;

        final IProcessor[] ps = new IProcessor[count];
        for (int i = 0; i < ps.length; ++i)
        {
            ps[i] = new ProcessorMock ("-" + i, i == exceptionalProcessorIndex, msecUntilException);
        }

        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            runProcessor (Processors.of (ps), msecDuration);
        }
    }

    private void runProcessor (final IProcessor p, final int msecRunDuration)
    {
        final Timer timer = new Timer ();
        final TimerTask timeoutAction = new TimerTask ()
        {
            @Override
            public void run ()
            {
                p.signalShutdown ();
            }
        };
        timer.schedule (timeoutAction, msecRunDuration);

        p.processUntilShutdown ();
    }

    //    class A
    //    {
    //        B m_b;
    //        int[] m_waste = new int[12345670];
    //    }
    //
    //    class B
    //    {
    //        A m_a;
    //        double[] m_waste = new double[45612300];
    //    }

    private static final int MSEC_RUN_DURATION = 500;
}
