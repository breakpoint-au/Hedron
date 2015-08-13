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
package au.com.breakpoint.hedron.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.function.Supplier;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.TimedScope;
import au.com.breakpoint.hedron.core.TimedScopeStatistics;
import au.com.breakpoint.hedron.core.context.FaultException;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;

public class TimedScopeTest
{
    // Removed this to shut up the javac compiler warning in Ant build

    //@Test (expected = FaultException.class)
    //public void testFaultException_Exception ()
    //{
    //    final TimedScope ts = TimedScope.getTimedScope ("Exception");
    //
    //    ts.execute (new Supplier<Integer> ()
    //    {
    //        @Override
    //        public Integer execute ()
    //        {
    //            Unsafe unsafe = null;
    //            try
    //            {
    //                final Field field = Unsafe.class.getDeclaredField ("theUnsafe");
    //                field.setAccessible (true);
    //                unsafe = (Unsafe) field.get (null);
    //            }
    //            catch (final NoSuchFieldException e)
    //            {
    //                // Propagate exception as unchecked fault up to the fault barrier.
    //                ThreadContext.throwFault (e);
    //            }
    //            catch (final SecurityException e)
    //            {
    //                // Propagate exception as unchecked fault up to the fault barrier.
    //                ThreadContext.throwFault (e);
    //            }
    //            catch (final IllegalArgumentException e)
    //            {
    //                // Propagate exception as unchecked fault up to the fault barrier.
    //                ThreadContext.throwFault (e);
    //            }
    //            catch (final IllegalAccessException e)
    //            {
    //                // Propagate exception as unchecked fault up to the fault barrier.
    //                ThreadContext.throwFault (e);
    //            }
    //
    //            //throw a checked exception without adding a "throws"
    //            unsafe.throwException (new IOException ());
    //            return 0;
    //        }
    //    });
    //
    //    assertTrue (false);
    //}

    @Test (expected = FaultException.class)
    public void testFaultException_RuntimeException ()
    {
        final TimedScope ts = TimedScope.getTimedScope ("RuntimeException");

        final Runnable task = () ->
        {
            throw new RuntimeException ();
        };
        ts.execute (task);

        assertTrue (false);
    }

    @Test (expected = FaultException.class)
    public void testFaultException_Throwable ()
    {
        final TimedScope ts = TimedScope.getTimedScope ("Throwable");

        final Runnable task = () ->
        {
            throw new Error ();
        };
        ts.execute (task);

        assertTrue (false);
    }

    @Test
    public void testGetResults ()
    {
        // Profiling wrapper. Timed wrapper for the inserts and the stored proc.
        final int msecFast = 50;
        final int msecSlow = 100;
        final int msecLimit = (msecFast + msecSlow) / 2;

        final Supplier<Integer> timedService1 = new Sleeper (msecFast);
        final Supplier<Integer> timedService2 = new Sleeper (msecSlow);

        final TimedScope ts1 = TimedScope.of (TimedScopeTest.class, "ts1");
        final int nrThreads = 50;
        final int nrTasks = nrThreads * 20;

        final Runnable task = () ->
        {
            ts1.execute (timedService1, msecLimit, null);
            ts1.execute (timedService2, msecLimit, null);
        };
        HcUtil.executeManyConcurrently (task, nrThreads, nrTasks);

        final TimedScope ts2 = TimedScope.of (TimedScopeTest.class, "ts2");
        ts2.execute (timedService1, msecLimit, null);
        ts2.execute (timedService2, msecLimit, null);

        Logging.logInfo (TimedScope.getResults (false));
    }

    @Test
    public void testGetTimedScope ()
    {
        final TimedScope ts1 = TimedScope.getTimedScope ("drhrrfrrfrr");
        final TimedScope ts2 = TimedScope.getTimedScope ("drhrr" + "frrfrr");
        assertSame (ts1, ts2);
    }

    @Test
    public void testInitialState ()
    {
        final TimedScope ts = TimedScope.of (TimedScopeTest.class, "ts");
        final TimedScopeStatistics s = ts.getStatistics ();
        assertEquals (0, s.getDurationMax ());
        assertEquals (Long.MAX_VALUE, s.getDurationMin ());
        assertEquals (0, s.getDurationTotal ());
        assertEquals (0, s.getExecutionsCount ());
        assertEquals (0, s.getSuccessfulExecutionsCount ());
    }

    private static final class Sleeper implements Supplier<Integer>
    {
        public Sleeper (final long msec)
        {
            m_msec = msec;
        }

        @Override
        public Integer get ()
        {
            try
            {
                Thread.sleep (m_msec);
            }
            catch (final InterruptedException e)
            {
                // Propagate exception as unchecked fault up to the fault barrier.
                ThreadContext.throwFault (e);
            }
            return 0;
        }

        private final long m_msec;
    }
}
