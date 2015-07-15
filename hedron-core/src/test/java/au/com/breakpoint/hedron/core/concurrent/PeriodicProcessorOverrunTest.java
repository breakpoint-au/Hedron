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

import static org.junit.Assert.assertTrue;
import java.util.Timer;
import java.util.TimerTask;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.concurrent.PeriodicProcessor;
import au.com.breakpoint.hedron.core.concurrent.PeriodicProcessor.OverrunPolicy;

public class PeriodicProcessorOverrunTest
{
    @Test
    public void testPeriodicOverrunImmediate ()
    {
        runProcessor (2000, 200, OverrunPolicy.RunImmediately);
    }

    @Test
    public void testPeriodicOverrunWait ()
    {
        runProcessor (2000, 200, OverrunPolicy.WaitForNextPeriod);
    }

    @Test
    public void testPeriodicOverrunWaitImmediateIntermittent ()
    {
        runIntermittentProcessor (3000, 200, OverrunPolicy.RunImmediately);
    }

    @Test
    public void testPeriodicOverrunWaitIntermittent ()
    {
        runIntermittentProcessor (3000, 200, OverrunPolicy.WaitForNextPeriod);
    }

    private void runIntermittentProcessor (final int durationMsec, final int periodMsec, final OverrunPolicy policy)
    {
        final Runnable task = new Runnable ()
        {
            @Override
            public void run ()
            {
                final long beforeMsec = System.currentTimeMillis () - m_startTimeMsec;
                final boolean shouldOverrun = m_i++ % 3 < 2;
                HcUtil.pause (shouldOverrun ? (periodMsec * 3) / 2 : periodMsec / 2);
                System.out.printf ("elapsed %s -> %s (%s)%n", beforeMsec, System.currentTimeMillis () - m_startTimeMsec,
                    shouldOverrun);
            }

            private int m_i = 0;

            private volatile long m_startTimeMsec = System.currentTimeMillis ();
        };
        final PeriodicProcessor p = new PeriodicProcessor ("testPeriodicOverrun", task, periodMsec);
        p.setOverrunPolicy (policy);

        runProcessor (p, durationMsec, "intermittent");
    }

    private void runProcessor (final int durationMsec, final int periodMsec, final OverrunPolicy policy)
    {
        final Runnable task = new Runnable ()
        {
            @Override
            public void run ()
            {
                final long beforeMsec = System.currentTimeMillis () - m_startTimeMsec;
                HcUtil.pause ((periodMsec * 3) / 2);
                System.out.printf ("elapsed %s -> %s%n", beforeMsec, System.currentTimeMillis () - m_startTimeMsec);
            }

            private volatile long m_startTimeMsec = System.currentTimeMillis ();
        };
        final PeriodicProcessor p = new PeriodicProcessor ("testPeriodicOverrun", task, periodMsec);
        p.setOverrunPolicy (policy);

        runProcessor (p, durationMsec, "");
    }

    private void runProcessor (final PeriodicProcessor p, final int durationMsec, final String title)
    {
        if (m_performTest)
        {
            System.out.printf ("------------------------------%n%s %s%n------------------------------%n",
                p.getOverrunPolicy (), title);

            final Timer timer = new Timer ();
            final TimerTask timeoutAction = new TimerTask ()
            {
                @Override
                public void run ()
                {
                    p.signalShutdown ();
                    p.awaitShutdownComplete ();
                }
            };
            timer.schedule (timeoutAction, durationMsec);

            p.processUntilShutdown ();
        }
        assertTrue (true);
    }

    private static final boolean m_performTest = false;
}
