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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Test;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.TimeLimitedIndexedCache;

public class TimeLimitedIndexedCacheTest
{
    @After
    public void dump ()
    {
        System.out.println (HcUtil.getSummaryData (true));
    }

    @Test
    public void testGet ()
    {
        final TimeLimitedIndexedCache<Integer, String> c = TimeLimitedIndexedCache
            .of (TimeLimitedIndexedCacheTest.class, "cacheName", context -> THE_STRING + context, 250);

        final String v1 = c.getValue (1);
        assertEquals (THE_STRING + 1, v1);
        final String v2 = c.getValue (2);
        assertEquals (THE_STRING + 2, v2);

        for (int i = 0; i < 10; ++i)
        {
            assertSame (v1, c.getValue (1));
            assertSame (v2, c.getValue (2));
        }

        // Expire the cached values.
        HcUtil.pause (300);

        // After expiry, objects should return the same values, but different object instances.
        final String vb1 = c.getValue (1);
        assertEquals (v1, vb1);
        assertTrue (vb1 != v1);

        final String vb2 = c.getValue (2);
        assertEquals (v2, vb2);
        assertTrue (vb2 != v2);

        for (int i = 0; i < 10; ++i)
        {
            assertSame (vb1, c.getValue (1));
            assertSame (vb2, c.getValue (2));
        }
    }

    // TODO 0 osx fails @Test
    public void testGetValueConcurrently ()
    {
        final int threadCount = 50;
        final int msecDuration = 200;
        final int msecExpiry = msecDuration - msecDuration / 4;

        final int[] countsMapEntries =
            {
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    15,
                    20,
                    50,
                    100
        };
        for (final int countMapEntries : countsMapEntries)
        {
            CalculatedString.m_countCalcs.set (0);
            CalculatedString.m_countGets.set (0);

            final TimeLimitedIndexedCache<Integer, CalculatedString> cache =
                TimeLimitedIndexedCache.of (TimeLimitedIndexedCacheTest.class, "cacheName", a ->
                {
                    // Lazy evaluate for the cache.
                    final CalculatedString cs = new CalculatedString ();
                    cs.m_s = String.valueOf ((int) a);
                    CalculatedString.m_countCalcs.incrementAndGet ();

                    return cs;
                } , msecExpiry);

            //    -Xms<size>    specifies the initial Java heap size and
            //    -Xmx<size>    the maximum Java heap size, eg  -Xmx128m

            final Runnable task = () ->
            {
                for (int j = 0; j < countMapEntries; ++j)
                {
                    cache.getValue (j);
                    CalculatedString.m_countGets.incrementAndGet ();
                }
            };
            HcUtil.executeManyConcurrentlyFor (task, threadCount, msecDuration, 0);

            // Should only have ever calculated once per value.
            final int calcCount = CalculatedString.m_countCalcs.get ();
            final int countExpectedCalc = countMapEntries * 2;
            //            System.out.printf ("Discrepancy %s for count %s performed %s calcs out of %s gets%n",
            //                calcCount - countExpectedCalc, countExpectedCalc, calcCount, CalculatedString.m_countGets.get ());

            assertEquals (countExpectedCalc, calcCount);
        }
    }

    private static class CalculatedString
    {
        @SuppressWarnings ("unused")
        private String m_s;

        private static AtomicInteger m_countCalcs = new AtomicInteger ();

        private static AtomicInteger m_countGets = new AtomicInteger ();
    }

    protected static final String THE_STRING = "abcd";
}
