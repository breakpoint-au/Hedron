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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.LogarithmicHistogram64;

public class LogarithmicHistogram64Test
{
    @Test
    public void testAccumulate ()
    {
        LogarithmicHistogram64 h = new LogarithmicHistogram64 ();

        long value = 1;
        for (int i = 0; i < 63; ++i, value *= 2)
        {
            assertTrue (HcUtil.safeEquals (h.getHistogramBins (), h.getHistogramBins ()));

            final LogarithmicHistogram64 hNew = h.accumulate (value);
            assertFalse (HcUtil.safeEquals (hNew.getHistogramBins (), h.getHistogramBins ()));

            h = hNew;
        }

        // Extremities too.
        h = h.accumulate (0);
        h = h.accumulate (Long.MAX_VALUE);

        assertArrayEquals (BITS_64, h.getHistogramBins ());
    }

    // Java language verification.
    @Test
    public void testArrayClone ()
    {
        final long[] a = new long[]
        {
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9
        };
        for (int i = 0; i < 10_000; ++i)
        {
            final long[] aClone = a.clone ();

            // Mutate the clone.
            for (int j = 0; j < aClone.length; ++j)
            {
                ++aClone[j];
            }

            for (int j = 0; j < aClone.length; ++j)
            {
                assertTrue (a[j] != aClone[j]);// prove true clone
            }
        }
    }

    @Test
    public void testToString1 ()
    {
        final long[] a = new long[]
        {
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9
        };

        LogarithmicHistogram64 h = new LogarithmicHistogram64 ();
        for (final long element : a)
        {
            h = h.accumulate (element);
        }

        final String s = h.toString ();
        assertEquals ("0-1:2; 2-3:2; 4-7:4; 8-15:2", s);
    }

    @Test
    public void testToString2 ()
    {
        final long[] a = new long[]
        {
                3,
                4,
                5,
                6,
                7,
                8,
                9
        };

        LogarithmicHistogram64 h = new LogarithmicHistogram64 ();
        for (final long element : a)
        {
            h = h.accumulate (element);
        }

        final String s = h.toString ();
        assertEquals ("2-3:1; 4-7:4; 8-15:2", s);
    }

    @Test
    public void testToString3 ()
    {
        final long[] a = new long[]
        {
                9
        };

        LogarithmicHistogram64 h = new LogarithmicHistogram64 ();
        for (final long element : a)
        {
            h = h.accumulate (element);
        }

        final String s = h.toString ();
        assertEquals ("8-15:1", s);
    }

    @Test
    public void testToString4 ()
    {
        final long[] a = new long[]
        {
                9223372036854775807L
        };

        LogarithmicHistogram64 h = new LogarithmicHistogram64 ();
        for (final long element : a)
        {
            h = h.accumulate (element);
        }

        final String s = h.toString ();
        assertEquals ("4611686018427387904-9223372036854775807:1", s);
    }

    private static final int[] BITS_64 = new int[]
    {
            2, // zero is also forced into the first bin
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            2
            // max value also falls into the last bin
    };
}
