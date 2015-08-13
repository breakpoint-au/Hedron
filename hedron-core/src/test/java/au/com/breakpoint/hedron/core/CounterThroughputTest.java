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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;
import org.junit.AfterClass;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.CounterThroughput;
import au.com.breakpoint.hedron.core.CounterThroughput.IClock;

public class CounterThroughputTest
{
    @Test
    public void testGetCounterThroughput ()
    {
        final CounterThroughput ts1 = CounterThroughput.getCounterThroughput ("drhrrfrrfrr", 1000);
        final CounterThroughput ts2 = CounterThroughput.getCounterThroughput ("drhrr" + "frrfrr", 1000);
        assertSame (ts1, ts2);
    }

    @Test
    public void testGetResults ()
    {
        // Profiling wrapper. Timed wrapper for the inserts and the stored proc.
        //        final CounterThroughput ts1 = CounterThroughput.newInstance ("ts1");
        //        final CounterThroughput ts2 = CounterThroughput.newInstance ("ts2");
        //
        //        for (int i = 0; i < 10; ++i)
        //        {
        //            ts1.increment ();
        //            if (i % 2 == 0)
        //            {
        //                ts2.increment ();
        //            }
        //        }
        //        assertEquals (10, ts1.get ());
        //        assertEquals (5, ts2.get ());
        //
        //        Logging.logInfo (CounterThroughput.getResults (false));
    }

    @Test
    public void testInitialState ()
    {
        //        final CounterThroughput ts = CounterThroughput.newInstance ("ts");
        //        assertEquals (0, ts.get ());
    }

    @Test
    public void testThroughputMinuteResolution ()
    {
        // 0: 2
        // 3: 1
        // 4: 1
        final long timesMinutes[] =
            {
                    // minutes 500-501: 3
                    500_001_123_456L,
                    500_010_123_456L,
                    500_999_123_456L,

                    // minutes 501-502: 0

                    // minutes 502-503: 0

                    // minutes 503-504: 4
                    503_001_123_456L,
                    503_010_123_456L,
                    503_998_123_456L,
                    503_999_123_456L,

                    // close off 503 minute
                    504_000_000_000L
        };

        final CounterThroughput e = CounterThroughput.of ("testThroughputMinuteResolution", 60 * 1000);
        e.setClock (new ClockSimulator (timesMinutes, 60));

        for (@SuppressWarnings ("unused")
        final long timesMinute : timesMinutes)
        {
            e.increment ();
        }

        final int[] h = e.getCounters ().getThroughputs ().getHistogramBins ();
        final int[] expecteds =
            {
                    //  Bin             min            max
                    // -----------------------------------
                    //     0              0              1 
                    //     1              2              3
                    //     2              4              7
                    //     3              8             15
                    //     4             16             31
                    2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        //System.out.printf ("expected: %s%n", Arrays.toString (expecteds));
        //System.out.printf ("actual:   %s%n", Arrays.toString (h));
        assertArrayEquals (expecteds, h);
    }

    @Test
    public void testThroughputSecondResolution ()
    {
        // 0: 2
        // 3: 1
        // 4: 1
        final long timesSeconds[] =
            {
                    // seconds 500-501: 3
                    500_001_123_456L,
                    500_010_123_456L,
                    500_999_123_456L,

                    // seconds 501-502: 0

                    // seconds 502-503: 0

                    // seconds 503-504: 4
                    503_001_123_456L,
                    503_010_123_456L,
                    503_998_123_456L,
                    503_999_123_456L,

                    // close off 503 second
                    504_000_000_000L
        };

        final CounterThroughput e = CounterThroughput.of ("testThroughputSecondResolution", 1000);
        e.setClock (new ClockSimulator (timesSeconds, 1));

        for (@SuppressWarnings ("unused")
        final long l : timesSeconds)
        {
            e.increment ();
        }

        final int[] h = e.getCounters ().getThroughputs ().getHistogramBins ();
        final int[] expecteds =
            {
                    //  Bin             min            max
                    // -----------------------------------
                    //     0              0              1 
                    //     1              2              3
                    //     2              4              7
                    //     3              8             15
                    //     4             16             31
                    2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        //System.out.printf ("expected: %s%n", Arrays.toString (expecteds));
        //System.out.printf ("actual:   %s%n", Arrays.toString (h));
        assertArrayEquals (expecteds, h);
    }

    private final class ClockSimulator implements IClock
    {
        public ClockSimulator (final long[] times, final int multiplier)
        {
            m_times = times;
            m_multiplier = multiplier;
        }

        @Override
        public long getTimeInNanoseconds ()
        {
            return m_times[m_i++] * m_multiplier;
        }

        private int m_i;

        private final int m_multiplier;

        private final long[] m_times;
    }

    @AfterClass
    public static void runAfterClass ()
    {
        System.out.println (HcUtil.getSummaryData (true));
    }
}
