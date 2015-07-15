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

import java.util.Timer;
import java.util.TimerTask;
import org.junit.Test;
import au.com.breakpoint.hedron.core.GenericFactory;
import au.com.breakpoint.hedron.core.IDataPersistence;
import au.com.breakpoint.hedron.core.IDataTransformation;
import au.com.breakpoint.hedron.core.ITransformationQueue;
import au.com.breakpoint.hedron.core.concurrent.IProcessor;
import au.com.breakpoint.hedron.core.concurrent.PeriodicTransformationProcessor;
import au.com.breakpoint.hedron.core.concurrent.RetryProcessor;
import au.com.breakpoint.hedron.core.context.FaultException;
import au.com.breakpoint.hedron.core.context.LoggingSilenceScope;

public class PeriodicTransformationProcessorTest
{
    @Test
    public void testProcessUntilShutdown ()
    {
        final PeriodicTransformationProcessor<Integer, String> p = getProcessor (false, 0);
        runProcessor (p, MSEC_RUN_DURATION);
    }

    @Test (expected = FaultException.class)
    public void testProcessUntilShutdownException ()
    {
        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            runProcessor (getProcessor (true, 2), MSEC_RUN_DURATION_EXCEPTION);
        }
    }

    @Test
    public void testProcessUntilShutdownExceptionsRetry ()
    {
        try (final LoggingSilenceScope ls = new LoggingSilenceScope ())
        {
            final RetryProcessor rp = new RetryProcessor ( () -> getProcessor (true, 1), 1);
            runProcessor (rp, MSEC_RUN_DURATION);
        }
    }

    private IDataPersistence<String> getExceptionalPersister (final int exceptionAfter)
    {
        final IDataPersistence<String> dp = new IDataPersistence<String> ()
        {
            @Override
            public void persist (final String d)
            {
                System.out.println (d);

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
        return dp;
    }

    private IDataPersistence<String> getPersister ()
    {
        final IDataPersistence<String> dp = d -> System.out.println (d);
        return dp;
    }

    private PeriodicTransformationProcessor<Integer, String> getProcessor (final boolean exceptional,
        final int exceptionAfter)
    {
        final ITransformationQueue<Integer> tq = () -> GenericFactory.newArrayList (23);
        final IDataTransformation<Integer, String> dt = a -> String.valueOf (a);
        final IDataPersistence<String> dp = exceptional ? getExceptionalPersister (exceptionAfter) : getPersister ();

        return PeriodicTransformationProcessor.of ("PeriodicTransformationProcessorTest", tq, dt, dp, MSEC_PERIOD);
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

    private static final int MSEC_PERIOD = 50;

    private static final int MSEC_RUN_DURATION = 200;

    private static final int MSEC_RUN_DURATION_EXCEPTION = 200 * 1;
}
