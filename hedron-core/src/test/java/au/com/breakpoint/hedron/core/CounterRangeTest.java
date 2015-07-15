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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;
import au.com.breakpoint.hedron.core.CounterRange;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.log.Logging;

public class CounterRangeTest
{
    @Test
    public void testGetCounterRange ()
    {
        final CounterRange ts1 = CounterRange.getCounterRange ("drhrrfrrfrr");
        final CounterRange ts2 = CounterRange.getCounterRange ("drhrr" + "frrfrr");
        assertSame (ts1, ts2);
    }

    @Test
    public void testGetResults ()
    {
        final CounterRange ts = CounterRange.of ("ts1");

        assertArrayEquals (new long[0], ts.getCounters ().getCounts ());

        ts.increment (4);
        checkExpected (ts, new long[]
        {
                4,
                1
        }, new long[]
        {
                4
        }, new long[]
        {
                1
        });

        ts.add (4, 2);
        checkExpected (ts, new long[]
        {
                4,
                3
        }, new long[]
        {
                4
        }, new long[]
        {
                3
        });

        ts.add (1, 5);
        checkExpected (ts, new long[]
        {
                1,
                5,
                4,
                3
        }, new long[]
        {
                1,
                4
        }, new long[]
        {
                5,
                3
        });

        ts.add (0, 7);
        checkExpected (ts, new long[]
        {
                0,
                7,
                1,
                5,
                4,
                3
        });

        ts.add (2, 2);
        checkExpected (ts, new long[]
        {
                0,
                7,
                1,
                5,
                2,
                2,
                4,
                3
        });

        ts.increment (3);
        checkExpected (ts, new long[]
        {
                0,
                7,
                1,
                5,
                2,
                2,
                3,
                1,
                4,
                3
        });

        ts.increment (5);
        checkExpected (ts, new long[]
        {
                0,
                7,
                1,
                5,
                2,
                2,
                3,
                1,
                4,
                3,
                5,
                1
        });

        ts.increment (50);
        checkExpected (ts, new long[]
        {
                0,
                7,
                1,
                5,
                2,
                2,
                3,
                1,
                4,
                3,
                5,
                1,
                50,
                1
        });

        ts.increment (49);
        checkExpected (ts, new long[]
        {
                0,
                7,
                1,
                5,
                2,
                2,
                3,
                1,
                4,
                3,
                5,
                1,
                49,
                1,
                50,
                1
        }, new long[]
        {
                0,
                1,
                2,
                3,
                4,
                5,
                49,
                50
        }, new long[]
        {
                7,
                5,
                2,
                1,
                3,
                1,
                1,
                1
        });

        Logging.logInfo ("%n%s", CounterRange.getResults (true));
    }

    @Test
    public void testInitialState ()
    {
        final CounterRange ts = CounterRange.of ("ts");
        assertEquals (0, ts.getCounters ().getCounts ().length);
    }

    private void checkExpected (final CounterRange ts, final long[] expected)
    {
        assertArrayEquals (expected, ts.getCounters ().getCounts ());
    }

    private void checkExpected (final CounterRange ts, final long[] expected, final long[] expectedCounts,
        final long[] expectedValues)
    {
        checkExpected (ts, expected);

        final E2<long[], long[]> cvs = ts.getCounters ().getEntries ();
        assertArrayEquals (expectedCounts, cvs.getE0 ());
        assertArrayEquals (expectedValues, cvs.getE1 ());
    }
}
